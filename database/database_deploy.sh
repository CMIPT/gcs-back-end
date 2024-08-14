#!/bin/bash

log_error () {
    echo -e "\e[31m[ERROR]: $1\e[0m"
    exit 1
}

log_info () {
    echo "[INFO]: $1"
}

# Set database connection information
DB_USER="test_deploy"  
DB_NAME="database_test13"
DB_HOST="localhost"
DB_PORT="5432"
DB_PASSWORD="root"
# Set the root directory where the SQL files are located
ROOT_DIR="."
SUB_DIR_SEQUENCE=(sequence table constraint function trigger)

TEMP_SQL_FILE="temp_sql_script.sql"

# Check if the database exists; if not, create it
if ! PGPASSWORD="$DB_PASSWORD" psql -U $DB_USER -h $DB_HOST -p $DB_PORT -lqt | cut -d \| -f 1 | grep -qw $DB_NAME; then
    log_info "Database $DB_NAME does not exist. Creating database..."
    if PGPASSWORD="$DB_PASSWORD" createdb -U $DB_USER -h $DB_HOST -p $DB_PORT $DB_NAME; then
        log_info "Database $DB_NAME created."
    else
        log_error "Error: Failed to create database $DB_NAME."
    fi
else
    log_info "Database $DB_NAME exists. Proceeding with script execution."
fi

rm -f $TEMP_SQL_FILE || log_error "Error: Failed to remove temporary SQL file."
touch $TEMP_SQL_FILE || log_error "Error: Failed to create temporary SQL file."

# Ensure function creation scripts are executed before trigger scripts
# Iterate through all .sql files in each subdirectory
for dir in "${SUB_DIR_SEQUENCE[@]}"; do
    for sql_file in "$ROOT_DIR/$dir"/*.sql; do
        echo "\i $sql_file" >> $TEMP_SQL_FILE || log_error "Error: Failed to write to temporary SQL file."
    done
done

# Execute the temporary SQL file in psql
if PGPASSWORD="$DB_PASSWORD" psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -f $TEMP_SQL_FILE; then
    log_info "SQL script executed successfully."
else
    log_error "Error: Failed to execute SQL script."
fi

# Remove the temporary SQL file after execution
rm $TEMP_SQL_FILE || log_error "Error: Failed to remove temporary SQL file."
