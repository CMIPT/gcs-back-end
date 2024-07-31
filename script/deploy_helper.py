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
    if res != 0:
        exit(res)


# TODO: add checker to check
def deploy_on_ubuntu(config):
    if config == None:
        return -1
    if config.runTest:
        res = os.system('mvn test')
        if res != 0:
            print("Test failed.")
            return res
    if config.deploy:
        res = subprocess.run('bash script/get_jar_position.sh', shell=True,
                             capture_output=True, text=True)
        if res.returncode != 0:
            return res.returncode
        package_path = res.stdout.strip()
        res = os.system('mvn package')
        if res != 0:
            return res

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
        if res != 0:
            return res
        create_systemd_service(config)
        if config.serviceEnable:
            res = os.system(f'sudo systemctl enable {config.serviceName}')
            if res != 0:
                return res
        else:
            res = os.system(f'sudo systemctl disable {config.serviceName}')
            if res != 0:
                return res
        res = os.system(f'sudo systemctl start {config.serviceName}')
        if res != 0:
            return res
        # TODO: finish deploy on docker


# TODO: add checker to check
def clean(config):
    os.system(f'sudo systemctl disable {config.serviceName}')
    os.system(f'sudo systemctl stop {config.serviceName}')
    if os.path.exists(f'/etc/systemd/system/{config.serviceName}.{config.serviceSuffix}'):
        os.system(
            f'sudo rm -rf /etc/systemd/system/{config.serviceName}.{config.serviceSuffix} && '
            f'sudo systemctl daemon-reload')
    os.system(f'sudo systemctl reset-failed {config.serviceName}')
    if os.path.exists(f'{config.serviceWorkingDirectory}'):
        os.system(f'sudo rm -rf {config.serviceWorkingDirectory}')
    if os.path.exists(f'{config.serviceStartJarFile}'):
        os.system(f'sudo rm -rf {config.serviceStartJarFile}')
    if os.path.exists(f'{config.servicePIDFile}'):
        os.system(f'sudo rm -rf {config.servicePIDFile}')
    if os.system(f"cat /etc/passwd | grep -w -E '^{config.serviceUser}'") == 0:
        os.system(f'sudo userdel {config.serviceUser}')
    os.system(f'mvn clean')


if __name__ == "__main__":
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
