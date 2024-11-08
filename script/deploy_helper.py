#!/usr/bin/env python
# -*- coding: utf-8 -*-

# NOTE: This script is used to deploy the project to the server.
#       This script should not be called directly.
# USAGE: python deploy_helper.py <config_file> <distro>

import argparse
import json
from types import SimpleNamespace
import os
import subprocess
import logging
import inspect

essential_packages = ['postgresql postgresql-client', 'openjdk-17-jdk-headless', 'maven',
                      'sudo', 'git', 'openssh-server']
apt_updated = False
message_tmp = '''\
The command below failed:
    {0}
Expected status code 0, got status code {1}
'''
application_config_file_path = 'src/main/resources/application.properties'


def setup_logger(log_level=logging.INFO):
    """
    Configure the global logging system.

    :param log_level: Set the logging level, defaulting to INFO.
    """
    logging.basicConfig(level=log_level,
                        format='%(asctime)s -%(levelname)s- in %(pathname)s:%(caller_lineno)d: '
                        '%(message)s',
                        datefmt='%Y-%m-%d %H:%M:%S')


def command_checker(status_code: int, message: str, expected_code: int = 0):
    """
    Check if the command execution status code meets the expected value.

    :param status_code: The actual status code of the command execution.
    :param message: The log message to be recorded.
    :param expected_code: The expected status code, defaulting to 0.
    """
    if status_code != expected_code:
        caller_frame = inspect.currentframe().f_back
        logging.error(message, extra={'caller_lineno': caller_frame.f_lineno})
        exit(status_code)


def log_info(message: str):
    caller_frame = inspect.currentframe().f_back
    logging.info(message, extra={'caller_lineno': caller_frame.f_lineno})


def log_debug(message: str):
    caller_frame = inspect.currentframe().f_back
    logging.debug(message, extra={'caller_lineno': caller_frame.f_lineno})


# open config_path and default_config_path
# union the result as an new obj
# return the new obj or None if error
def load_config_file_as_obj(config_path: str, default_config_path: str):
    try:
        with open(config_path, 'r') as f:
            config = json.load(f, object_hook=lambda d: SimpleNamespace(**d))
        with open(default_config_path, 'r') as f:
            default_config = json.load(f, object_hook=lambda d: SimpleNamespace(**d))
    except Exception as e:
        command_checker(1, f"Error: {e}")
        return None
    for key in default_config.__dict__:
        if not hasattr(config, key):
            setattr(config, key, getattr(default_config, key))
    return config


def parse_iterable_into_str(iterable, sep=" "):
    return sep.join(iterable).strip()


def write_content_to_file(content, file_path, mode='w'):
    try:
        with open(file_path, mode) as f:
            f.write(content)
    except Exception as e:
        command_checker(1, f"Error: {e}")


def create_systemd_service(config):
    assert(config != None)
    exec_start = parse_iterable_into_str([config.serviceStartJavaCommand] +
                                         config.serviceStartJavaArgs +
                                         [config.serviceStartJarFile])
    wanted_by = parse_iterable_into_str(config.serviceWantedBy)
    after = parse_iterable_into_str(config.serviceAfter)
    service_full_path = f'{config.serviceSystemdDirectory}/{config.serviceName}{config.serviceSuffix}'
    gcs_file_content = f"""\
[Unit]
Description={config.serviceDescription}
After={after}

[Service]
PIDFile={config.servicePIDFile}
User={config.serviceUser}
WorkingDirectory={config.serviceWorkingDirectory}
Restart={config.serviceRestartPolicy}
RestartSec={config.serviceRestartDelaySeconds}
ExecStart={exec_start}

[Install]
WantedBy={wanted_by}
"""
    write_content_to_file(gcs_file_content, service_full_path)


def create_sys_v_init_service(config):
    try:
        with open('script/service_tmp.sh', 'r') as f:
            service_content = f.read()
    except Exception as e:
        command_checker(1, f"Error: {e}")
        return

    header = f'''#!/bin/env bash
NAME={config.serviceName}
SCRIPT="{parse_iterable_into_str([config.serviceStartJavaCommand] +
config.serviceStartJavaArgs + [config.serviceStartJarFile])}"
RUNAS={config.serviceUser}
PIDFILE={config.servicePIDFile}
LOGFILE={config.serviceLogFile}
'''
    service_content = header + service_content
    log_debug(f"service_content:\n {service_content}")
    write_content_to_file(
        service_content, f'{config.serviceSysVInitDirectory}/{config.serviceName}')
    res = os.system(f'chmod +x {config.serviceSysVInitDirectory}/{config.serviceName}')
    command_checker(
        res, f"Failed to chmod +x {config.serviceSysVInitDirectory}/{config.serviceName}")


def apt_install_package(name):
    global apt_updated
    if not apt_updated:
        res = os.system(f'apt update')
        command_checker(res, 'Failed to update apt')
        apt_updated = True
    res = os.system(f'apt install -y {name}')
    command_checker(res, f'Failed to install {name}')


def deploy_with_systemd(config):
    create_systemd_service(config)
    if config.serviceStartWithBoot:
        command = f'systemctl enable {config.serviceName}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    command = f'systemctl start {config.serviceName}'
    res = os.system(command)
    message = message_tmp.format(command, res)
    command_checker(res, message)


def deploy_with_sys_v_init(config):
    create_sys_v_init_service(config)
    res = os.system(f'service {config.serviceName} start')
    command_checker(res, f"Failed to start {config.serviceName}")
    if config.serviceStartWithBoot:
        res = os.system(f'update-rc.d {config.serviceName} defaults')
        command_checker(res, f"Failed to start {config.serviceName} with boot")


def activate_profile(config):
    profile_format = f"spring.profiles.active={parse_iterable_into_str(config.profiles, sep=',')}\n"
    log_debug(f"Profile format: {profile_format}")
    write_content_to_file(profile_format, application_config_file_path, 'a')


def config_datasource(config):
    datasource_map_config = {
        "username": config.postgresqlUserName,
        "password": config.postgresqlUserPassword,
        "url": f"jdbc:postgresql://{config.postgresqlHost}:{config.postgresqlPort}/{config.postgresqlDatabaseName}",
        "stat-view-servlet.login-username": config.druidLoginUsername,
        "stat-view-servlet.login-password": config.druidLoginPassword,
    }
    datasource_format = "spring.datasource.druid.{0}={1}"
    log_debug(f"Datasource format: {datasource_format}")
    content = ""
    for key, value in datasource_map_config.items():
        log_debug(f"Datasource config: {datasource_format.format(key, value)}")
        content += datasource_format.format(key, value) + '\n'
    write_content_to_file(content, application_config_file_path, 'a')


def init_database(config):
    create_or_update_user("postgres", config.postgresUserPassword)
    res = os.system(f'service postgresql start')
    command_checker(res, "Failed to start postgresql")
    # check if there is a user in database
    process = subprocess.Popen(['su', '-c',
                                f'psql -c "SELECT * FROM pg_user WHERE '
                                f'usename=\'{config.postgresqlUserName}\';"',
                                'postgres'],
                               stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                               stderr=subprocess.PIPE, text=True, cwd='/tmp')
    assert(process.stdin is not None)
    process.stdin.write(f'{config.postgresUserPassword}')
    process.stdin.flush()
    out, err = process.communicate()
    command_checker(process.returncode, f"Failed to check the user in database: {err}")
    log_debug(f"Postgresql user info:\n {out}")
    if out.find(config.postgresqlUserName) != -1:
        # update the password
        log_info(f"Updating the password of {config.postgresqlUserName} in database")
        process = subprocess.Popen(['su', '-c',
                                    f'psql -c "ALTER USER {config.postgresqlUserName} '
                                    f'WITH PASSWORD \'{config.postgresqlUserPassword}\';"',
                                    'postgres'],
                                   stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                   stderr=subprocess.PIPE, text=True, cwd='/tmp')
        assert(process.stdin is not None)
        process.stdin.write(f'{config.postgresUserPassword}')
        process.stdin.flush()
        out, err = process.communicate()
        command_checker(process.returncode, f"Failed to update the password in database: {err}")
        log_info(f"Password of {config.postgresqlUserName} in database has been updated")
    else:
        # create the user in database
        log_info(f"Creating the user in database: {config.postgresqlUserName}")
        process = subprocess.Popen(['su', '-c',
                                    f'psql -c "CREATE USER {config.postgresqlUserName} '
                                    f'WITH PASSWORD \'{config.postgresqlUserPassword}\';"',
                                    'postgres'],
                                   stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                   stderr=subprocess.PIPE, text=True, cwd='/tmp')
        assert(process.stdin is not None)
        process.stdin.write(f'{config.postgresUserPassword}')
        process.stdin.flush()
        out, err = process.communicate()
        command_checker(process.returncode, f"Failed to create the user in database: {err}")
        log_info(f"User {config.postgresqlUserName} has been created in database")
    # check if there is a database
    process = subprocess.Popen(['su', '-c',
                                f'psql -c "SELECT * FROM pg_database WHERE '
                                f'datname=\'{config.postgresqlDatabaseName}\';"',
                                'postgres'],
                               stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                               stderr=subprocess.PIPE, text=True, cwd='/tmp')
    assert(process.stdin is not None)
    process.stdin.write(f'{config.postgresUserPassword}')
    process.stdin.flush()
    out, err = process.communicate()
    command_checker(process.returncode, f"Failed to check the database: {err}")
    log_debug(f"Postgresql database info:\n {out}")
    if out.find(config.postgresqlDatabaseName) == -1:
        # create the database
        log_info(f"Creating the database: {config.postgresqlDatabaseName}")
        process = subprocess.Popen(['su', '-c',
                                    f'psql -c "CREATE DATABASE {config.postgresqlDatabaseName};"',
                                    'postgres'],
                                   stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                                   stderr=subprocess.PIPE, text=True, cwd='/tmp')
        assert(process.stdin is not None)
        process.stdin.write(f'{config.postgresUserPassword}')
        process.stdin.flush()
        out, err = process.communicate()
        command_checker(process.returncode, f"Failed to create the database: {err}")
        log_info(f"Database {config.postgresqlDatabaseName} has been created")
        run_shell_script = True
    else:
        run_shell_script = False
    # grant the user
    log_info(f"Granting the user in database: {config.postgresqlUserName}")
    process = subprocess.Popen(['su', '-c',
                                f'psql -c "GRANT ALL PRIVILEGES ON DATABASE '
                                f'{config.postgresqlDatabaseName} TO {config.postgresqlUserName};"',
                                'postgres'],
                               stdin=subprocess.PIPE, stdout=subprocess.PIPE,
                               stderr=subprocess.PIPE, text=True, cwd='/tmp')
    assert(process.stdin is not None)
    process.stdin.write(f'{config.postgresUserPassword}')
    process.stdin.flush()
    out, err = process.communicate()
    command_checker(process.returncode, f"Failed to grant the user in database: {err}")
    log_info(f"User {config.postgresqlUserName} has been granted in database")
    # Do not run the shell script if the database had been created before
    if run_shell_script:
        res = os.system(f'bash database/database_deploy.sh {config.postgresqlUserName} '
                        f'{config.postgresqlDatabaseName} {config.postgresqlHost} '
                        f'{config.postgresqlPort}  {config.postgresqlUserPassword}')
        command_checker(res, f"Failed to deploy the database")
    config_datasource(config)


def create_or_update_user(username, password, homeDirectory=None):
    if username == None or username == "":
        return
    if os.system(f"cat /etc/passwd | grep -w -E '^{username}'") != 0:
        # use -m to create the home directory for user
        command = f'useradd -m '
        if homeDirectory is not None:
            command += f'-d {homeDirectory} '
        command += username
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    elif homeDirectory is not None:  # update the home directory
        command = f'usermod -d {homeDirectory} {username}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    if password == None or password == "":
        command = f'passwd -d {username}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    else:
        process = subprocess.Popen('chpasswd', stdin=subprocess.PIPE,
                                   stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        assert(process.stdin is not None)
        process.stdin.write(f'{username}:{password}')
        process.stdin.flush()
        process.communicate()
        command_checker(process.returncode, f"Failed to update the password of {username}")


def write_other_config(config):
    other_config_map = {
        "frontEndUrl": "front-end.url",
        "gitServerDomain": "git.server.domain",
        "gitServerPort": "git.server.port",
        "gitUserName": "git.user.name",
        "gitHomeDirectory": "git.home.directory",
        "md5Salt": "md5.salt",
        "staticPathPattern": "spring.mvc.static-path-pattern",
        "staticLocations": "spring.web.resources.static-locations",
        "redisHost": "spring.redis.host",
        "redisPort": "spring.redis.port",
        "springMailHost": "spring.mail.host",
        "springMailPort": "spring.mail.port",
        "springMailUsername": "spring.mail.username",
        "springMailPassword": "spring.mail.password",
        "springMailProtocol": "spring.mail.protocol",
        "springMailDefaultEncoding": "spring.mail.default-encoding",
    }
    if config.frontEndUrl is None:
        config.frontEndUrl = ""
    if config.staticPathPattern is None:
        config.staticPathPattern = ""
    if config.staticLocations is None:
        config.staticLocations = ""
    else:
        config.staticLocations = ['file:' + location for location in config.staticLocations]
        config.staticLocations = parse_iterable_into_str(config.staticLocations, ',')
    content = ""
    for key, value in other_config_map.items():
        content += f"{value}={getattr(config, key)}\n"
        log_debug(f"Other config: {value}={getattr(config, key)}")
    write_content_to_file(content, application_config_file_path, 'a')
    write_content_to_file("gitolite.admin.repository.path="
                          f"{config.serviceUserHomeDirectory}/gitolite-admin\n",
                          application_config_file_path, 'a')


def init_gitolite(config):
    # to make gitolite work properly, we must remove the git user and the home directory
    delete_user(config.gitUserName)
    if os.path.exists(config.gitHomeDirectory):
        command = f'rm -rf {config.gitHomeDirectory}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    create_or_update_user(config.gitUserName, config.gitUserPassword, config.gitHomeDirectory)
    # clone the gitolite repository
    command = ("su -c 'git clone https://github.com/sitaramc/gitolite "
               f"{config.gitHomeDirectory}/gitolite' {config.gitUserName}")
    log_debug(f"Clone gitolite command: {command}")
    command_checker(os.system(command), "Failed to clone gitolite")
    # install the gitolite
    command = (f"su -c 'mkdir -p {config.gitHomeDirectory}/bin; "
               f"{config.gitHomeDirectory}/gitolite/install -to "
               f"{config.gitHomeDirectory}/bin' {config.gitUserName}")
    log_debug(f"Install gitolite command: {command}")
    command_checker(os.system(command), f"Failed to install gitolite")
    command = (f"cp {config.serviceUserHomeDirectory}/.ssh/id_rsa.pub "
               f"{config.gitHomeDirectory}/{config.serviceUser}.pub")
    log_debug(f"Copy id_rsa.pub command: {command}")
    command_checker(os.system(command),
                    f"Failed to copy {config.serviceUserHomeDirectory}/.ssh/id_rsa.pub "
                    f"to {config.gitHomeDirectory}/{config.serviceUser}.pub")
    command = (f'chown {config.gitUserName}:{config.gitUserName} '
               f'{config.gitHomeDirectory}/{config.serviceUser}.pub')
    log_debug(f"Chown command: {command}")
    command_checker(os.system(command),
                    f"Failed to chown {config.gitHomeDirectory}/{config.serviceUser}.pub")
    command = (f"su -c '"
               f"{config.gitHomeDirectory}/bin/gitolite setup -pk "
               f"{config.gitHomeDirectory}/{config.serviceUser}.pub' {config.gitUserName}")
    log_debug(f"Setup gitolite command: {command}")
    command_checker(os.system(command), f"Failed to setup gitolite")
    # clone the gitolite-admin repository
    if os.path.exists(f'{config.serviceUserHomeDirectory}/gitolite-admin'):
        res = os.system(f'rm -rf {config.serviceUserHomeDirectory}/gitolite-admin')
        command_checker(res,
                        f"Failed to remove old {config.serviceUserHomeDirectory}/gitolite-admin")
    command = (f"su -c \"GIT_SSH_COMMAND='ssh -o StrictHostKeyChecking=no' "
               f"git clone ssh://{config.gitUserName}"
               f"@localhost:{config.localSshdPort}/gitolite-admin "
               f"{config.serviceUserHomeDirectory}/gitolite-admin\" {config.serviceUser}")
    log_debug(f"Clone gitolite-admin command: {command}")
    command_checker(os.system(command), f"Failed to clone gitolite-admin")
    command = (f"su -c 'mkdir -p {config.serviceUserHomeDirectory}/gitolite-admin/conf/gitolite.d/user' "
               f"{config.serviceUser}")
    log_debug(f"Create usr directory command: {command}")
    command_checker(os.system(command), f"Failed to create usr directory in gitolite-admin/conf")
    command = (f"su -c 'mkdir -p {config.serviceUserHomeDirectory}/gitolite-admin/conf/gitolite.d/repository' "
               f"{config.serviceUser}")
    log_debug(f"Create repository directory command: {command}")
    command_checker(os.system(command),
                    f"Failed to create repository directory in gitolite-admin/conf")
    content = f'''
repo gitolite-admin
    RW+ = {config.serviceUser}
repo testing
    R = @all
include "gitolite.d/user/*.conf"
include "gitolite.d/repository/*.conf"
@all_public_repo =
repo @all_public_repo
    R = @all
'''
    write_content_to_file(
        content, f'{config.serviceUserHomeDirectory}/gitolite-admin/conf/gitolite.conf')
    # create the usr directory in gitolite-admin/conf
    # configure the username and email for gitolite-admin
    command = (f"su -c 'git -C {config.serviceUserHomeDirectory}/gitolite-admin "
               f"config user.name \"{config.adminName}\"' {config.serviceUser}")
    log_debug(f"Config username command: {command}")
    command_checker(os.system(command), f"Failed to config username")
    command = (f"su -c 'git -C {config.serviceUserHomeDirectory}/gitolite-admin "
               f"config user.email \"{config.adminEmail}\"' {config.serviceUser}")
    log_debug(f"Config email command: {command}")
    command_checker(os.system(command), f"Failed to config email")
    command = (f"su -c \"git -C {config.serviceUserHomeDirectory}/gitolite-admin "
               f"commit -am 'Init the gitolite-admin'\" {config.serviceUser}")
    log_debug(f"Commit command: {command}")
    command_checker(os.system(command), f"Failed to commit the change")
    command = (f"su -c 'git -C {config.serviceUserHomeDirectory}/gitolite-admin push'"
               f" {config.serviceUser}")
    log_debug(f"Push command: {command}")
    command_checker(os.system(command), f"Failed to push the change")


def install_redis():
    # check if redis has been installed
    if os.system('command -v redis-cli') == 0:
        return
    command = '''
    apt-get install lsb-release curl gpg &&
    curl -fsSL https://packages.redis.io/gpg | sudo gpg --dearmor -o /usr/share/keyrings/redis-archive-keyring.gpg &&
    chmod 644 /usr/share/keyrings/redis-archive-keyring.gpg &&
    echo "deb [signed-by=/usr/share/keyrings/redis-archive-keyring.gpg] https://packages.redis.io/deb $(lsb_release -cs) main" > /etc/apt/sources.list.d/redis.list &&
    apt-get update &&
    apt-get install -y redis'''
    res = os.system(command)
    command_checker(res, "Failed to install redis")


def deploy_on_ubuntu(config):
    assert(config != None)
    if os.path.exists(application_config_file_path):
        res = os.system(f'rm -rf {application_config_file_path}')
        command_checker(res, f"Failed to remove {application_config_file_path}")
    if config.serviceType != 'systemd':
        essential_packages.remove('systemd')
    apt_install_package(parse_iterable_into_str(essential_packages))
    install_redis()
    command = 'service postgresql restart'
    res = os.system(command)
    message = message_tmp.format(command, res)
    command_checker(res, message)
    command = 'service ssh restart'
    res = os.system(command)
    message = message_tmp.format(command, res)
    command_checker(res, message)
    command = 'service sshd restart'
    res = os.system(command)
    message = message_tmp.format(command, res)
    command_checker(res, message)
    init_database(config)
    create_or_update_user(config.serviceUser, config.serviceUserPassword)
    if os.path.exists(f'{config.serviceUserHomeDirectory}/.ssh'):
        res = os.system(f'rm -rf {config.serviceUserHomeDirectory}/.ssh')
        command_checker(res, f"Failed to remove {config.serviceUserHomeDirectory}/.ssh")
    res = os.system(
        f"su -c \"ssh-keygen -f {config.serviceUserHomeDirectory}/.ssh/id_rsa -N ''\" {config.serviceUser}")
    command_checker(res, f"Failed to generate ssh key for {config.serviceUser}")
    init_gitolite(config)
    # let the service user can use git, rm and tee commands as the git user without password
    sudoers_entry = f"{config.serviceUser} ALL=({config.gitUserName}) NOPASSWD: /usr/bin/rm"
    res = subprocess.run(
        f"echo '{sudoers_entry}' | tee /etc/sudoers.d/{config.serviceUser}", shell=True)
    command_checker(res.returncode, f"Failed to create /etc/sudoers.d/{config.serviceUser}")
    res = subprocess.run(f"chmod 440 /etc/sudoers.d/{config.serviceUser}", shell=True)
    command_checker(res.returncode, f"Failed to chmod 440 /etc/sudoers.d/{config.serviceUser}")
    activate_profile(config)
    write_other_config(config)
    res = subprocess.run('bash script/get_jar_position.sh', shell=True,
                         capture_output=True, text=True)
    command_checker(res.returncode, res.stderr)
    package_path = res.stdout.strip()
    command = f'mvn package -Dmaven.test.skip=true'
    res = os.system(command)
    message = message_tmp.format(command, res)
    command_checker(res, message)

    if not os.path.exists(os.path.dirname(config.serviceStartJarFile)):
        command = f'mkdir -p {os.path.dirname(config.serviceStartJarFile)}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    command = f'cp {package_path} {config.serviceStartJarFile}'
    res = os.system(command)
    message = message_tmp.format(command, res)
    command_checker(res, message)
    if config.serviceType == 'sys-init-v':
        deploy_with_sys_v_init(config)
    elif config.serviceType == 'systemd':
        deploy_with_systemd(config)
    else:
        raise ValueError(f"Invalid service type: {config.serviceType}")


def delete_user(username):
    if username == None or username == "":
        return
    if os.system(f"cat /etc/passwd | grep -w -E '^{username}'") == 0:
        command = f'userdel {username}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)


def clean(config):
    if config.deployWithDocker:
        res = os.system(f"docker stop {config.dockerName}")
        command_checker(res, f"Failed to stop {config.dockerName}")
        return
    if config.serviceType == 'systemd':
        command = f'systemctl disable {config.serviceName}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
        command = f'systemctl stop {config.serviceName}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
        if os.path.exists(f'{config.serviceSystemdDirectory}/{config.serviceName}{config.serviceSuffix}'):
            command = f'''rm -rf {config.serviceSystemdDirectory}/{config.serviceName}{config.serviceSuffix} && \\
        systemctl daemon-reload'''
            res = os.system(command)
            message = message_tmp.format(command, res)
            command_checker(res, message)
        command = f'systemctl reset-failed {config.serviceName}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    elif config.serviceType == 'sys-init-v':
        command = f'service {config.serviceName} uninstall'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    else:
        raise ValueError(f"Invalid service type: {config.serviceType}")
    if os.path.exists(f'{config.serviceWorkingDirectory}'):
        command = f'rm -rf {config.serviceWorkingDirectory}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    if os.path.exists(f'{config.serviceStartJarFile}'):
        command = f'rm -rf {config.serviceStartJarFile}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    if os.path.exists(f'{config.servicePIDFile}'):
        command = f'rm -rf {config.servicePIDFile}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    if os.path.exists(f'/etc/sudoers.d/{config.serviceUser}'):
        command = f'rm -rf /etc/sudoers.d/{config.serviceUser}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    if config.deleteGitUser:
        delete_user(config.gitUserName)
    if config.deleteServiceUser:
        delete_user(config.serviceUser)
    command = f'mvn clean'
    res = os.system(command)
    message = message_tmp.format(command, res)
    command_checker(res, message)


def get_cli_args():
    parser = argparse.ArgumentParser(
        description="Deploy the project when the environment is ready.",
        formatter_class=argparse.RawTextHelpFormatter,
        usage="""
    deploy_helper.py [OPTION]... [--config-path PATH] [--distro DISTRO]
or: deploy_helper.py [OPTION]... --clean
or: deploy_helper.py [OPTION]... [--log-level LEVEL] [--in-docker]"""
    )
    parser.add_argument('--config-path', nargs='?', default='../config.json',
                        type=str, help="Default to '../config.json'. Path to config JSON file.")
    parser.add_argument('--distro', nargs='?', default='ubuntu',
                        type=str, help="Default to 'ubuntu'. Set linux distribution.")
    parser.add_argument('--default-config-path', nargs='?', default='../config_default.json',
                        type=str, help="Default to '../config_default.json'. Path to default config JSON file.")
    parser.add_argument('--clean', action='store_true',
                        help="Default to false. Clean up the project.")
    parser.add_argument('--in-docker', action='store_true',
                        help="Default to false. Whether or not deploy in docker.")
    parser.add_argument('--log-level', nargs='?', default='INFO',
                        type=str, help=("""\
Default to 'INFO'.
Set the logging level. Available levels are: 
'DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL'.
- DEBUG: Detailed information, useful for diagnosing issues.
  Typically only enabled during development or troubleshooting.
- INFO: General information about the application's normal operations.
  Used to confirm that things are working as expected.
- WARNING: Indicates a potential issue or unexpected situation.
  The application is still running but may encounter problems soon.
- ERROR: A significant problem occurred, preventing some functionality
  from working. The application is still running but encountered a failure.
- CRITICAL: A severe error occurred, causing the application to
  stop or severely impact functionality. Immediate attention is required.
"""))
    return parser.parse_args()


def deploy_with_docker(config):
    res = os.system(f'cd 3rdparty/docker-script && '
                    f'bash create_docker.sh -n {config.dockerName} -d {config.dockerImage} '
                    f'-w {int(config.dockerWithGpu)} '
                    f'-p {parse_iterable_into_str(config.dockerPortMapping, sep = " -p ")}')
    command_checker(res, f"Failed to create docker: {config.dockerName}")
    res = os.system(f'docker exec -it {config.dockerName} '
                    f'bash -c "apt update && apt install -y python-is-python3"')
    command_checker(res, f"Failed to install python-is-python3 in docker: {config.dockerName}")
    res = os.system(f'docker cp . {config.dockerName}:{config.dockerSrcPath}')
    command_checker(res, f"Failed to copy the repository to docker: {config.dockerName}")
    res = os.system(f'docker exec -it {config.dockerName} '
                    f'bash -c "cd {config.dockerSrcPath} && '
                    f'python script/deploy_helper.py --config-path {config.configPath} '
                    f'--default-config-path {config.defaultConfigPath} '
                    f'--distro {config.distro} --in-docker"')
    command_checker(res, f"Failed to deploy in docker: {config.dockerName}")
    return


def check_args(config):
    # TODO: add more checks
    required_fields = ['springMailHost', 'springMailPort',
                       'springMailUsername', 'springMailPassword']
    for field in required_fields:
        if not hasattr(config, field):
            raise ValueError(f"Missing required field: {field}")


def main():
    args = get_cli_args()
    if args.log_level.upper() not in logging._nameToLevel:
        raise ValueError(f"Invalid log level: {args.log_level}")
    config = load_config_file_as_obj(args.config_path, args.default_config_path)
    assert(config != None)
    setup_logger(getattr(logging, config.deployLogLevel.upper()))
    setattr(config, 'inDocker', args.in_docker)
    setattr(config, 'configPath', args.config_path)
    setattr(config, 'defaultConfigPath', args.default_config_path)
    setattr(config, 'distro', args.distro)
    check_args(config)
    if args.clean:
        clean(config)
    elif not args.in_docker and config.deployWithDocker:
        deploy_with_docker(config)
    elif args.distro == 'ubuntu':
        deploy_on_ubuntu(config)


if __name__ == "__main__":
    main()
