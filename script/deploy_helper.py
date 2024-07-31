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


def load_json_file_as_obj(file_path: str):
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            data = json.load(file, object_hook=lambda d: SimpleNamespace(**d))
    except Exception as e:
        print(f"Error: {e}")
        return None
    return data


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
        if os.path.exists(f'/etc/init.d/{config.serviceName}'):
            res = os.system(f'sudo service {config.serviceName} stop && '
                            f'sudo rm /etc/init.d/{config.serviceName} && '
                            f'sudo systemctl daemon-reload')
            if res != 0:
                return res
        res = subprocess.run('script/get_jar_position.sh', shell=True,
                             capture_output=True, text=True)
        if res.returncode != 0:
            return res.returncode
        package_path = res.stdout.strip()
        res = os.system('mvn package')
        if res != 0:
            return res

        if os.system(f"cat /etc/passwd | grep -w -E '^{config.serviceUser}'") != 0:
            os.system(f'sudo useradd {config.serviceUser}')
            process = subprocess.Popen(['sudo', 'chpasswd'], stdin=subprocess.PIPE,
                                       stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
            assert(process.stdin is not None)
            process.stdin.write(f'{config.serviceUser}:{config.servicePassword}')
            process.stdin.flush()
            process.communicate()

        res = os.system(
            f'sudo chown {config.serviceUser}:{config.serviceUser} {package_path}')
        if res != 0:
            return res
        res = os.system(f'sudo ln -s {package_path} /etc/init.d/{config.serviceName}')
        if res != 0:
            return res
        res = os.system(
            f'sudo systemctl daemon-reload && sudo service {config.serviceName} start &')
        if res != 0:
            return res
        # TODO: finish deploy on docker


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="Deploy the project when the environment is ready.")
    parser.add_argument('-f', '--file_path', nargs='?', default='../config.json',
                        type=str, help="Path to the JSON file")
    parser.add_argument('-d', '--distro', nargs='?', default='ubuntu',
                        type=str, help="Linux distribution")
    args = parser.parse_args()
    if args.distro == 'ubuntu':
        exit(deploy_on_ubuntu(load_json_file_as_obj(args.file_path)))
