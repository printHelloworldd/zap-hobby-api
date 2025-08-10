#!/bin/bash
set -e

# Connection parameters
export PGPASSWORD=$POSTGRES_PASSWORD

DB_HOST=${POSTGRES_HOST:-postgres}
DB_PORT=${POSTGRES_PORT:-5432}
DB_USER=${POSTGRES_USER:-postgres}
DB_NAME=${POSTGRES_DB:-hobbies_db}
INPUT_DIR=${INPUT_DIR:-/tmp/exports}
MODE=${MODE:-truncate}  # append, truncate, or upsert
CONFLICT_COLUMN=${CONFLICT_COLUMN:-id}  # for upsert mode

while getopts "h:p" opt; do
  case $opt in
    h) DB_HOST="$OPTARG" ;;     # -h host
    p) DB_PORT="$OPTARG" ;;      # -p port
    h) # -h — help
       echo "Usage: $0 -h <hostname> -p <port>"
       exit 0 ;;
    \?) # unknown flag
        echo "Unknown argument: -$OPTARG"
        exit 1 ;;
  esac
done

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Method for color output
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Shows connection parameters
echo "=== Database Import Configuration ==="
echo "Database: $DB_NAME"
echo "Host: $DB_HOST:$DB_PORT"
echo "User: $DB_USER"
echo "Input Directory: $INPUT_DIR"
echo "Mode: $MODE"
echo "=================================="

# Checks the directory
if [ ! -d "$INPUT_DIR" ]; then
    log_error "Directory $INPUT_DIR does not exist"
    exit 1
fi

# Checks DB connection
log_info "Checking database connection..."
until pg_isready -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME > /dev/null 2>&1; do
    echo "PostgreSQL is unavailable - sleeping"
    sleep 2
done
log_info "Database connection successful!"

# Import method depending on mode
import_table() {
    local csv_file=$1
    local table_name=$2
    
    case $MODE in
        "truncate")
            log_info "Truncating table $table_name before import"
            psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "TRUNCATE TABLE $table_name CASCADE;"
            psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "\COPY $table_name FROM '$csv_file' WITH CSV HEADER;"
            ;;
        "upsert")
            # Creates a temporary table
            log_info "Using upsert mode with conflict resolution on column: $CONFLICT_COLUMN"
            temp_table="${table_name}_temp_$(date +%s)"
            
            # Gets table structure
            psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
                CREATE TEMP TABLE $temp_table AS SELECT * FROM $table_name WHERE FALSE;
            "
            
            # Loads data into a temporary table
            psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "\COPY $temp_table FROM '$csv_file' WITH CSV HEADER;"
            
            # Performs an upsert
            psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "
                INSERT INTO $table_name 
                SELECT * FROM $temp_table
                ON CONFLICT ($CONFLICT_COLUMN) DO UPDATE SET
                    $(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "
                        SELECT string_agg(column_name || ' = EXCLUDED.' || column_name, ', ')
                        FROM information_schema.columns 
                        WHERE table_name = '$table_name' 
                        AND table_schema = 'public' 
                        AND column_name != '$CONFLICT_COLUMN'
                    ");
            "
            ;;
        "append"|*)
            log_info "Using append mode (default)"
            psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -c "\COPY $table_name FROM '$csv_file' WITH CSV HEADER;"
            ;;
    esac
}

# Gets CSV files list
CSV_FILES=$(find $INPUT_DIR -name "*.csv" -type f | sort)

if [ -z "$CSV_FILES" ]; then
    log_warn "No CSV files found in $INPUT_DIR"
    exit 1
fi

# Calculates the total number of files
total_files=$(echo "$CSV_FILES" | wc -l)
current_file=0

log_info "Found $total_files CSV files to import"

# Imports each file
for csv_file in $CSV_FILES; do
    current_file=$((current_file + 1))
    table_name=$(basename "$csv_file" .csv)
    
    echo "[$current_file/$total_files] Processing: $(basename $csv_file) -> table: $table_name"
    
    # Checks if the table exists
    table_exists=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "
        SELECT COUNT(*) 
        FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = '$table_name';
    ")
    table_exists=$(echo $table_exists | tr -d ' ')
    
    if [ "$table_exists" -eq 0 ]; then
        log_warn "Table $table_name does not exist. Skipping..."
        continue
    fi
    
    # Checks if CSV file is not empty
    if [ ! -s "$csv_file" ]; then
        log_warn "CSV file $csv_file is empty. Skipping..."
        continue
    fi
    
    # Gets rows in CSV (excluding header)
    csv_lines=$(tail -n +2 "$csv_file" | wc -l)
    if [ "$csv_lines" -eq 0 ]; then
        log_warn "CSV file $csv_file contains only header. Skipping..."
        continue
    fi
    
    # Number of records before import
    count_before=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM $table_name;")
    count_before=$(echo $count_before | tr -d ' ')
    
    # Imports data
    if import_table "$csv_file" "$table_name"; then
        # Number of records after import
        count_after=$(psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "SELECT COUNT(*) FROM $table_name;")
        count_after=$(echo $count_after | tr -d ' ')
        
        case $MODE in
            "truncate")
                log_info "Imported $count_after records (table was truncated)"
                ;;
            *)
                imported_records=$((count_after - count_before))
                log_info "Imported $imported_records new records (total: $count_after)"
                ;;
        esac
    else
        log_error "Failed to import $csv_file"
    fi
done

log_info "Import completed! All CSV files from $INPUT_DIR have been processed."