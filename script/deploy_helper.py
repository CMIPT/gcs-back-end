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
                      'openjdk-17-jdk-headless', 'maven', 'systemd']
sudo_cmd = os.popen('command -v sudo').read().strip()
apt_updated = False
message_tmp = '''\
The command below failed:
    {0}
Expected status code 0, got status code {1}
'''


def setup_logger(log_level=logging.INFO):
    """
    Configure the global logging system.

    :param log_level: Set the logging level, defaulting to INFO.
    """
    logging.basicConfig(level=log_level,
                        format='%(asctime)s -%(levelname)s- in %(pathname)s:%(caller_lineno)d: %(message)s',
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
    exec_start = parse_iterable_into_str(
        [config.serviceStartJavaCommand] + config.serviceStartJavaArgs + [config.serviceStartJarFile])
    wanted_by = parse_iterable_into_str(config.serviceWantedBy)
    after = parse_iterable_into_str(config.serviceAfter)
    service_full_path = f'{config.serviceSystemdDirectory}/{config.serviceName}.{config.serviceSuffix}'
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

    res = os.system(
        f"echo '{service_content}' | {sudo_cmd} tee {config.serviceSysVInitDirectory}/{config.serviceName}")
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


def deploy_on_ubuntu(config):
    assert(config != None)
    if config.inDocker:
        essential_packages.remove('systemd')
    apt_install_package(parse_iterable_into_str(essential_packages))
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

    if config.deploy:
        if os.system(f"cat /etc/passwd | grep -w -E '^{config.serviceUser}'") != 0:
            command = f'{sudo_cmd} useradd {config.serviceUser}'
            res = os.system(command)
            message = message_tmp.format(command, res)
            command_checker(res, message)
            if config.serviceUserPassword == None or config.serviceUserPassword == "":
                command = f'{sudo_cmd} passwd -d {config.serviceUser}'
                res = os.system(command)
                message = message_tmp.format(command, res)
                command_checker(res, message)
            else:
                process = subprocess.Popen(['sudo', 'chpasswd'], stdin=subprocess.PIPE,
                                           stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
                assert(process.stdin is not None)
                process.stdin.write(f'{config.serviceUser}:{config.serviceUserPassword}')
                process.stdin.flush()
                process.communicate()

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
    if os.path.exists(f'{config.serviceSystemdDirectory}/{config.serviceName}.{config.serviceSuffix}'):
        command = f'''{sudo_cmd} rm -rf {config.serviceSystemdDirectory}/{config.serviceName}.{config.serviceSuffix} && \\
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
    if os.system(f"cat /etc/passwd | grep -w -E '^{config.serviceUser}'") == 0:
        command = f'{sudo_cmd} userdel {config.serviceUser}'
        res = os.system(command)
        message = message_tmp.format(command, res)
        command_checker(res, message)
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
    setup_logger(getattr(logging, args.log_level.upper()))
    config = load_config_file_as_obj(args.config_path, args.default_config_path)
    assert(config != None)
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
