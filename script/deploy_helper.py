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


def setup_logger(log_level=logging.INFO):
    """
    配置全局日志系统。

    :param log_level: 设置日志记录级别，默认为 INFO 级别。
    """
    logging.basicConfig(level=log_level,
                        format='%(asctime)s - %(levelname)s - %(message)s',
                        datefmt='%Y-%m-%d %H:%M:%S')


def command_checker(status_code: int, message: str, expected_code: int = 0):
    """
    检查命令执行的状态码是否符合预期。

    :param status_code: 实际命令执行的状态码
    :param message: 要记录的日志信息
    :param expected_code: 预期的状态码，默认为0
    """
    if status_code != expected_code:
        logging.error(f"Command failed: {message} Expected status code {expected_code}, got status code {status_code}.")
        exit(status_code)


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
        print(f"Error: {e}")
        return None
    for key in default_config.__dict__:
        if not hasattr(config, key):
            setattr(config, key, getattr(default_config, key))
    return config


def parse_iterable_into_str(iterable, sep=" "):
    res = ""
    for it in iterable:
        res += f'{it}{sep}'
    res.strip()
    return res


def create_systemd_service(config):
    if config == None:
        return -1
    exec_start = parse_iterable_into_str(
        [config.serviceStartJavaCommand] + config.serviceStartJavaArgs + [config.serviceStartJarFile])
    wanted_by = parse_iterable_into_str(config.serviceWantedBy)
    after = parse_iterable_into_str(config.serviceAfter)
    service_full_path = f'/etc/systemd/system/{config.serviceName}.{config.serviceSuffix}'
    gcs_file_content = \
        f"""\
[Unit]
Description={config.serviceDescription}
After={after}

[Service]
PIDFile={config.servicePIDFile}
User={config.serviceUser}
WorkingDirectory={config.serviceWorkingDirectory}
Restart={config.serviceRestartPolicy}
RestartSec={config.serviceRestartDelaySecond}
ExecStart={exec_start}

[Install]
WantedBy={wanted_by}
"""
    res = os.system(
        f'echo "{gcs_file_content}" | sudo tee {service_full_path}')
    command_checker(res, f"echo \"{gcs_file_content}\" | sudo tee {service_full_path} failed", 0)


# TODO: add checker to check
def deploy_on_ubuntu(config):
    if config == None:
        return -1
    if config.runTest:
        res = os.system('mvn test')
        command_checker(res, "mvn test failed.", 0)
    if config.deploy:
        res = subprocess.run('bash script/get_jar_position.sh', shell=True,
                             capture_output=True, text=True)
        command_checker(res.returncode, "script/get_jar_position.sh failed.", 0)
        package_path = res.stdout.strip()
        res = os.system('mvn package')
        command_checker(res, "mvn package failed.", 0)

        if os.system(f"cat /etc/passwd | grep -w -E '^{config.serviceUser}'") != 0:
            os.system(f'sudo useradd {config.serviceUser}')
            if config.serviceUserPassword == None or config.serviceUserPassword == "":
                os.system(f'sudo passwd -d {config.serviceUser}')
            else:
                process = subprocess.Popen(['sudo', 'chpasswd'], stdin=subprocess.PIPE,
                                           stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
                assert(process.stdin is not None)
                process.stdin.write(f'{config.serviceUser}:{config.serviceUserPassword}')
                process.stdin.flush()
                process.communicate()

        if not os.path.exists(os.path.dirname(config.serviceStartJarFile)):
            os.system(f'sudo mkdir -p {os.path.dirname(config.serviceStartJarFile)}')
        res = os.system(f'sudo cp {package_path} {config.serviceStartJarFile}')
        command_checker(res, f"sudo cp {package_path} failed", 0)
        create_systemd_service(config)
        if config.serviceEnable:
            res = os.system(f'sudo systemctl enable {config.serviceName}')
            command_checker(res, f"sudo systemctl enable {config.serviceName} failed", 0)
        else:
            res = os.system(f'sudo systemctl disable {config.serviceName}')
            command_checker(res, f"sudo systemctl disable {config.serviceName} failed", 0)
        res = os.system(f'sudo systemctl start {config.serviceName}')
        command_checker(res, f"sudo systemctl start {config.serviceName} failed", 0)
        # TODO: finish deploy on docker


# TODO: add checker to check
def clean(config):
    res = os.system(f'sudo systemctl disable {config.serviceName}')
    command_checker(res, f"sudo systemctl disable {config.serviceName} failed", 0)
    res = os.system(f'sudo systemctl stop {config.serviceName}')
    command_checker(res, f"sudo systemctl stop {config.serviceName} failed", 0)
    if os.path.exists(f'/etc/systemd/system/{config.serviceName}.{config.serviceSuffix}'):
        res = os.system(
            f'sudo rm -rf /etc/systemd/system/{config.serviceName}.{config.serviceSuffix} && '
            f'sudo systemctl daemon-reload')
        command_checker(res, f"sudo rm -rf /etc/systemd/system/{config.serviceName}.{config.serviceSuffix} && \
                             sudo systemctl daemon-reload failed", 0)
    res = os.system(f'sudo systemctl reset-failed {config.serviceName}')
    command_checker(res, f"sudo systemctl reset-failed {config.serviceName} failed", 0)
    if os.path.exists(f'{config.serviceWorkingDirectory}'):
        res = os.system(f'sudo rm -rf {config.serviceWorkingDirectory}')
        command_checker(res, f"sudo rm -rf {config.serviceWorkingDirectory} failed", 0)
    if os.path.exists(f'{config.serviceStartJarFile}'):
        res = os.system(f'sudo rm -rf {config.serviceStartJarFile}')
        command_checker(res, f"sudo rm -rf {config.serviceStartJarFile} failed", 0)
    if os.path.exists(f'{config.servicePIDFile}'):
        res = os.system(f'sudo rm -rf {config.servicePIDFile}')
        command_checker(res, f"sudo rm -rf {config.servicePIDFile} failed", 0)
    if os.system(f"cat /etc/passwd | grep -w -E '^{config.serviceUser}'") == 0:
        res = os.system(f'sudo userdel {config.serviceUser}')
        command_checker(res, f"sudo userdel {config.serviceUser} failed", 0)
    res = os.system(f'mvn clean')
    command_checker(res, f"mvn clean failed", 0)


if __name__ == "__main__":
    setup_logger()
    parser = argparse.ArgumentParser(
        description="Deploy the project when the environment is ready.")
    parser.add_argument('--config-path', nargs='?', default='../config.json',
                        type=str, help="Path to the JSON file")
    parser.add_argument('--distro', nargs='?', default='ubuntu',
                        type=str, help="Linux distribution")
    parser.add_argument('--default-config-path', nargs='?', default='../config_default.json',
                        type=str, help="Linux distribution")
    parser.add_argument('--clean', action='store_true', help="Clean up the project")
    args = parser.parse_args()
    if args.clean:
        clean(load_config_file_as_obj(args.config_path, args.default_config_path))
    elif args.distro == 'ubuntu':
        exit(deploy_on_ubuntu(load_config_file_as_obj(args.config_path, args.default_config_path)))
