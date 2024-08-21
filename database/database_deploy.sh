#!/bin/bash

log_error () {
    echo -e "\e[31m[ERROR]: $1\e[0m"
    exit 1
}

log_info () {
    echo "[INFO]: $1"
}

# Set database connection information
DB_USER=$1
DB_NAME=$2
DB_HOST=$3
DB_PORT=$4
DB_PASSWORD=$5
# Set the root directory where the SQL files are located
ROOT_DIR="database"
SUB_DIR_SEQUENCE=(table constraint function trigger)

TEMP_SQL_FILE="temp_sql_script.sql"

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
if PGPASSWORD="$DB_PASSWORD" psql -U "$DB_USER" -d "$DB_NAME" -h "$DB_HOST" -p "$DB_PORT" -f $TEMP_SQL_FILE; then
    log_info "SQL script executed successfully."
else
    log_error "Error: Failed to execute SQL script."
fi

# Remove the temporary SQL file after execution
rm $TEMP_SQL_FILE || log_error "Error: Failed to remove temporary SQL file."
