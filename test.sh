#!/bin/bash

cd "$(dirname "$0")" || exit 1

if [ ! -f ".env" ]; then
    echo "Error: .env file not found."
    exit 1
fi

SUPABASE_DB_URL=$(grep '^SUPABASE_DB_URL=' .env | cut -d '=' -f2-)

SUPABASE_DB_URL="${SUPABASE_DB_URL%\"}"
SUPABASE_DB_URL="${SUPABASE_DB_URL#\"}"
SUPABASE_DB_URL="${SUPABASE_DB_URL%\'}"
SUPABASE_DB_URL="${SUPABASE_DB_URL#\'}"
SUPABASE_DB_URL="${SUPABASE_DB_URL//$'\r'/}"

if [ -z "$SUPABASE_DB_URL" ]; then
    echo "Error: SUPABASE_DB_URL could not be loaded from .env"
    exit 1
fi

export SPRING_DATASOURCE_URL="$SUPABASE_DB_URL"

echo "Database environment loaded."
echo "Running tests..."

./gradlew test