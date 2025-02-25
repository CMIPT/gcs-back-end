#!/usr/bin/env bash
# USAGE: bash clean_ubuntu.sh [config_file]

config_file=${1:-"config.json"}

# TODO: reuse the log_error and log_info functions from deploy_ubuntu.sh
log_error () {
    echo -e "\e[31m[ERROR]: $1\e[0m"
    exit 1
}

log_info () {
    echo "[INFO]: $1"
}

log_info "Cleaning up..."
python script/deploy_helper.py \
    --config-path "$config_file" \
    --clean \
    --distro ubuntu \
    --default-config-path ./config_default.json || \
    log_error "Failed to run deploy_helper.py for cleaning up the environment"
