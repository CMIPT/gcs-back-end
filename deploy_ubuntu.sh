#!/usr/bin/env bash

config_file=${1:-"config.json"}

log_error () {
    echo -e "\e[31m[ERROR]: $1\e[0m"
    exit 1
}

log_info () {
    echo "[INFO]: $1"
}

log_info "Config file: ${config_file}"

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
install_package python-is-python3

log_info "Deploying..."
python script/deploy_helper.py \
    --config-path "$config_file" \
    --distro ubuntu \
    --default-config-path ./config_default.json || \
    log_error "Failed to run deploy_helper.py for deploying the environment"
