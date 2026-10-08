#!/bin/bash

cd "$(dirname "$0")" || exit 1

if [ ! -f ".env" ]; then
    echo "Error: .env file not found."
    exit 1
fi

SUPABASE_DB_URL=$(grep '^SUPABASE_DB_URL=' .env | cut -d '=' -f2-)

# Remove surrounding quotes if present
SUPABASE_DB_URL="${SUPABASE_DB_URL%\"}"
SUPABASE_DB_URL="${SUPABASE_DB_URL#\"}"
SUPABASE_DB_URL="${SUPABASE_DB_URL%\'}"
SUPABASE_DB_URL="${SUPABASE_DB_URL#\'}"

# Remove possible Windows carriage return
SUPABASE_DB_URL="${SUPABASE_DB_URL//$'\r'/}"

if [ -z "$SUPABASE_DB_URL" ]; then
    echo "Error: SUPABASE_DB_URL could not be loaded from .env"
    exit 1
fi

export SPRING_DATASOURCE_URL="$SUPABASE_DB_URL"

echo "SUPABASE_DB_URL loaded successfully."
echo "URL begins with: ${SUPABASE_DB_URL:0:30}..."
echo "Starting Spring Boot..."

./gradlew bootRun