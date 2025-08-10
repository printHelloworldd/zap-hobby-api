#!/bin/bash
set -e

# Connection parameters
DB_HOST=${POSTGRES_HOST:-localhost}
DB_PORT=${POSTGRES_PORT:-5332}
DB_USER=${POSTGRES_USER:-postgres}
DB_NAME=${POSTGRES_DB:-hobbies_db}
OUTPUT_DIR=${OUTPUT_DIR:-./exports}

# Creates a directory for export
mkdir -p $OUTPUT_DIR

echo "Exporting all tables from database $DB_NAME..."

# Gets list of all tables
TABLES=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "
    SELECT tablename 
    FROM pg_tables 
    WHERE schemaname = 'public'
    ORDER BY tablename;
")

# Exports each table
for table in $TABLES; do
    # Remove spaces
    table=$(echo $table | tr -d ' ')
    
    if [ ! -z "$table" ]; then
        echo "Exporting table: $table"
        
        psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "\COPY $table TO '$OUTPUT_DIR/${table}.csv' WITH CSV HEADER;"
        
        # Shows the number of records
        count=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM $table;")
        echo "  -> $count records exported to ${table}.csv"
    fi
done

echo "Export completed! Files saved in: $OUTPUT_DIR"