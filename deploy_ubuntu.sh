#!/usr/bin/env bash

config_file=${1:-"config.json"}

echo "Config file: ${config_file}"

log_error () {
    echo -e "\e[31m[ERROR]: $1\e[0m"
    exit 1
}

log_info () {
    echo "[INFO]: $1"
}

apt_updated=false
install_package() {
    local sudo_cmd
    sudo_cmd=$(command -v sudo)
    if [ "$apt_updated" = false ]; then
        ${sudo_cmd} apt-get update
        apt_updated=true
    fi
    ${sudo_cmd} apt-get install -y "$1" || log_error "Failed to install $1"
}

# install essential packages
if ! command -v python || ! command -v python3; then
    install_package python-is-python3
fi
if ! command -v psql; then
    install_package postgresql
    install_package postgresql-client
fi
if ! dpkg -l | grep jdk-17; then
    install_package openjdk-17-jdk-headless
fi
if ! command -v mvn; then
    install_package maven
fi
if ! command -v systemctl; then
    install_package systemd
fi

log_info "Deploying..."
python script/deploy_helper.py \
    --config-path "$config_file" \
    --distro ubuntu \
    --default-config-path ./config_default.json || \
    log_error "Failed to run deploy_helper.py for deploying the environment"
