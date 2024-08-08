#!/bin/bash

# Set database connection information
DB_USER="postgres"  
DB_NAME="database_test"
DB_HOST="localhost"
DB_PORT="5432"
DB_PASSWORD="root"  

# Set the root directory where the SQL files are located
ROOT_DIR="./"

TEMP_SQL_FILE="temp_sql_script.sql"

# Check if the database exists; if not, create it
if ! PGPASSWORD="$DB_PASSWORD" psql -U $DB_USER -h $DB_HOST -p $DB_PORT -lqt | cut -d \| -f 1 | grep -qw $DB_NAME; then
    echo "Database $DB_NAME does not exist. Creating database..."
    if PGPASSWORD="$DB_PASSWORD" createdb -U $DB_USER -h $DB_HOST -p $DB_PORT $DB_NAME; then
        echo "Database $DB_NAME created."
    else
        echo "Error: Failed to create database $DB_NAME."
        exit 1
    fi
else
    echo "Database $DB_NAME exists. Proceeding with script execution."
fi

# Create or empty the temporary SQL file
> $TEMP_SQL_FILE

# Ensure function creation scripts are executed before trigger scripts
# Iterate through all .sql files in each subdirectory
for dir in sequence table constraint; do
    for sql_file in "$ROOT_DIR/$dir"/*.sql; do
        echo "\i $sql_file" >> $TEMP_SQL_FILE
    done
done

# Add function creation scripts before trigger scripts
for sql_file in "$ROOT_DIR/trigger/update_gmt_updated_column.sql"; do
    echo "\i $sql_file" >> $TEMP_SQL_FILE
done

for sql_file in "$ROOT_DIR/trigger/all_table_trigger.sql"; do
    echo "\i $sql_file" >> $TEMP_SQL_FILE
done

# Execute the temporary SQL file in psql
if PGPASSWORD="$DB_PASSWORD" psql -U $DB_USER -d $DB_NAME -h $DB_HOST -p $DB_PORT -f $TEMP_SQL_FILE; then
    echo "SQL script executed successfully."
else
    echo "Error: Failed to execute SQL script."
    exit 1
fi

# Remove the temporary SQL file after execution
rm $TEMP_SQL_FILE
