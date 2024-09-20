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

essential_packages = ['python-is-python3', 'postgresql postgresql-client',
                      'openjdk-17-jdk-headless', 'maven', 'systemd', 'sudo']
sudo_cmd = os.popen('command -v sudo').read().strip()
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
    command = f'echo "{gcs_file_content}" | {sudo_cmd} tee {service_full_path}'
    res = os.system(command)
    message = message_tmp.format(command, res)
    command_checker(res, message)


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

    res = os.system(f"echo '{service_content}' | "
                    f"{sudo_cmd} tee {config.serviceSysVInitDirectory}/{config.serviceName}")
    command_checker(res, f"Failed to create {config.serviceSysVInitDirectory}/{config.serviceName}")
    res = os.system(f'{sudo_cmd} chmod +x {config.serviceSysVInitDirectory}/{config.serviceName}')
    command_checker(
        res, f"Failed to chmod +x {config.serviceSysVInitDirectory}/{config.serviceName}")

    if logging.getLogger().level == logging.DEBUG:
        try:
            with open(f'{config.serviceSysVInitDirectory}/{config.serviceName}', 'r') as f:
                log_debug(f"Service content:\n {f.read()}")
        except Exception as e:
            command_checker(1, f"Error: {e}")
            return


def apt_install_package(name):
    global apt_updated
    if not apt_updated:
        res = os.system(f'{sudo_cmd} apt update')
        command_checker(res, 'Failed to update apt')
        apt_updated = True
    res = os.system(f'{sudo_cmd} apt install -y {name}')
    command_checker(res, f'Failed to install {name}')


def deploy_with_systemd(config):
    create_systemd_service(config)
    if config.serviceEnable:
        command = f'{sudo_cmd} systemctl enable {config.serviceName}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    else:
        command = f'{sudo_cmd} systemctl disable {config.serviceName}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    command = f'{sudo_cmd} systemctl start {config.serviceName}'
    res = os.system(command)
    message = message_tmp.format(command, res)
    command_checker(res, message)


def deploy_with_sys_v_init(config):
    create_sys_v_init_service(config)
    res = os.system(f'{sudo_cmd} service {config.serviceName} start')
    command_checker(res, f"Failed to start {config.serviceName}")
    if config.serviceStartWithBoot:
        res = os.system(f'{sudo_cmd} update-rc.d {config.serviceName} defaults')
        command_checker(res, f"Failed to start {config.serviceName} with boot")


def activate_profile(config):
    profile_format = f"spring.profiles.active={parse_iterable_into_str(config.profiles, sep=',')}"
    log_debug(f"Profile format: {profile_format}")
    try:
        with open(application_config_file_path, 'a') as f:
            f.write(profile_format + '\n')
    except Exception as e:
        command_checker(1, f"Error: {e}")


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
    try:
        with open(application_config_file_path, 'a') as f:
            for key, value in datasource_map_config.items():
                f.write(datasource_format.format(key, value) + '\n')
                log_debug(f"Datasource config: {datasource_format.format(key, value)}")
    except Exception as e:
        command_checker(1, f"Error: {e}")


def init_database(config):
    create_or_update_user("postgres", config.postgresUserPassword)
    res = os.system(f'{sudo_cmd} service postgresql start')
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


def create_or_update_user(username, password):
    if username == None or username == "":
        return
    if os.system(f"cat /etc/passwd | grep -w -E '^{username}'") != 0:
        # use -m to create the home directory for user
        command = f'{sudo_cmd} useradd -m {username}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    if password == None or password == "":
        command = f'{sudo_cmd} passwd -d {username}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    else:
        process = subprocess.Popen([sudo_cmd, 'chpasswd'], stdin=subprocess.PIPE,
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
        "gitUserName": "git.user.name",
        "gitRepositoryDirectory": "git.repository.directory",
        "gitRepositorySuffix": "git.repository.suffix",
    }
    try:
        with open(application_config_file_path, 'a') as f:
            for key, value in other_config_map.items():
                f.write(f"{value}={getattr(config, key)}\n")
                log_debug(f"Other config: {value}={getattr(config, key)}")
    except Exception as e:
        command_checker(1, f"Error: {e}")


def deploy_on_ubuntu(config):
    assert(config != None)
    if config.inDocker:
        essential_packages.remove('systemd')
    apt_install_package(parse_iterable_into_str(essential_packages))
    init_database(config)
    activate_profile(config)
    write_other_config(config)
    skip_test = ""
    if config.skipTest:
        skip_test = "-Dmaven.test.skip=true"
    res = subprocess.run('bash script/get_jar_position.sh', shell=True,
                         capture_output=True, text=True)
    command_checker(res.returncode, res.stderr)
    package_path = res.stdout.strip()
    command = f'mvn package {skip_test}'
    res = os.system(command)
    message = message_tmp.format(command, res)
    command_checker(res, message)
    create_or_update_user(config.gitUserName, config.gitUserPassword)
    create_or_update_user(config.serviceUser, config.serviceUserPassword)
    # let the service user can use git command as the git user without password
    sudoers_entry = f"{config.serviceUser} ALL=(git) NOPASSWD: /usr/bin/git"
    try:
        res = subprocess.run(f"echo '{sudoers_entry}' | {sudo_cmd} tee /etc/sudoers.d/{config.serviceUser}", shell=True);
        command_checker(res.returncode, f"Failed to create /etc/sudoers.d/{config.serviceUser}")
        res = subprocess.run(f"{sudo_cmd} chmod 440 /etc/sudoers.d/{config.serviceUser}", shell=True)
        command_checker(res.returncode, f"Failed to chmod 440 /etc/sudoers.d/{config.serviceUser}")
    except subprocess.CalledProcessError as e:
        command_checker(1, f"Error: {e}")

    if config.deploy:
        if not os.path.exists(os.path.dirname(config.serviceStartJarFile)):
            command = f'{sudo_cmd} mkdir -p {os.path.dirname(config.serviceStartJarFile)}'
            res = os.system(command)
            message = message_tmp.format(command, res)
            command_checker(res, message)
        command = f'{sudo_cmd} cp {package_path} {config.serviceStartJarFile}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
        if config.inDocker:
            deploy_with_sys_v_init(config)
        else:
            deploy_with_systemd(config)


def delete_user(username):
    if username == None or username == "":
        return
    if os.system(f"cat /etc/passwd | grep -w -E '^{username}'") == 0:
        command = f'{sudo_cmd} userdel {username}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)

def clean(config):
    if config.deployWithDocker:
        res = os.system(f"docker stop {config.dockerName}")
        command_checker(res, f"Failed to stop {config.dockerName}")
        res = os.system(f"docker rm {config.dockerName}")
        command_checker(res, f"Failed to remove {config.dockerName}")
        return
    command = f'{sudo_cmd} systemctl disable {config.serviceName}'
    res = os.system(command)
    message = message_tmp.format(command, res)
    command_checker(res, message)
    command = f'{sudo_cmd} systemctl stop {config.serviceName}'
    res = os.system(command)
    message = message_tmp.format(command, res)
    command_checker(res, message)
    if os.path.exists(f'{config.serviceSystemdDirectory}/{config.serviceName}{config.serviceSuffix}'):
        command = f'''{sudo_cmd} rm -rf {config.serviceSystemdDirectory}/{config.serviceName}{config.serviceSuffix} && \\
    {sudo_cmd} systemctl daemon-reload'''
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    command = f'{sudo_cmd} systemctl reset-failed {config.serviceName}'
    res = os.system(command)
    message = message_tmp.format(command, res)
    command_checker(res, message)
    if os.path.exists(f'{config.serviceWorkingDirectory}'):
        command = f'{sudo_cmd} rm -rf {config.serviceWorkingDirectory}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    if os.path.exists(f'{config.serviceStartJarFile}'):
        command = f'{sudo_cmd} rm -rf {config.serviceStartJarFile}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    if os.path.exists(f'{config.servicePIDFile}'):
        command = f'{sudo_cmd} rm -rf {config.servicePIDFile}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
    if os.path.exists(f'/etc/sudoers.d/{config.serviceUser}'):
        command = f'{sudo_cmd} rm -rf /etc/sudoers.d/{config.serviceUser}'
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
        description="Deploy the project when the environment is ready.")
    parser.add_argument('--config-path', nargs='?', default='../config.json',
                        type=str, help="Path to the JSON file")
    parser.add_argument('--distro', nargs='?', default='ubuntu',
                        type=str, help="Linux distribution")
    parser.add_argument('--default-config-path', nargs='?', default='../config_default.json',
                        type=str, help="Linux distribution")
    parser.add_argument('--clean', action='store_true', help="Clean up the project")
    parser.add_argument('--log-level', nargs='?', default='INFO',
                        type=str, help=(
                            "Set the logging level. Possible values are: "
                            "'DEBUG', 'INFO', 'WARNING', 'ERROR', 'CRITICAL'. "
                            "Default is 'INFO'.\n"
                            "- DEBUG: Detailed information, typically of interest only when diagnosing problems.\n"
                            "- INFO: Confirmation that things are working as expected.\n"
                            "- WARNING: An indication that something unexpected happened, or indicative of some problem in the near future. The software is still working as expected.\n"
                            "- ERROR: Due to a more serious problem, the software has not been able to perform some function.\n"
                            "- CRITICAL: A very serious error, indicating that the program itself may be unable to continue running."
                        ))
    parser.add_argument('--in-docker', action='store_true', help="Whether or not deploy in docker")
    return parser.parse_args()


def deploy_in_docker(config):
    res = os.system(f'cd 3rdparty/docker-script && '
                    f'bash create_docker.sh -n {config.dockerName} -d {config.dockerImage} '
                    f'-w {int(config.dockerWithGpu)} '
                    f'-p {parse_iterable_into_str(config.dockerPortMapping, sep = " -p ")}')
    command_checker(res, f"Failed to create docker: {config.dockerName}")
    res = os.system(f'{sudo_cmd} docker exec -it {config.dockerName} '
                    f'bash -c "sudo apt update && sudo apt install -y python-is-python3"')
    command_checker(res, f"Failed to install python-is-python3 in docker: {config.dockerName}")
    res = os.system(f'{sudo_cmd} docker cp . {config.dockerName}:{config.dockerSrcPath}')
    command_checker(res, f"Failed to copy the repository to docker: {config.dockerName}")
    res = os.system(f'{sudo_cmd} docker exec -it {config.dockerName} '
                    f'bash -c "cd {config.dockerSrcPath} && '
                    f'python script/deploy_helper.py --config-path {config.configPath} '
                    f'--default-config-path {config.defaultConfigPath} '
                    f'--distro {config.distro} --in-docker"')
    command_checker(res, f"Failed to deploy in docker: {config.dockerName}")
    return


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
    if args.clean:
        clean(config)
    elif not args.in_docker and config.deployWithDocker:
        deploy_in_docker(config)
    elif args.distro == 'ubuntu':
        deploy_on_ubuntu(config)


if __name__ == "__main__":
    main()
