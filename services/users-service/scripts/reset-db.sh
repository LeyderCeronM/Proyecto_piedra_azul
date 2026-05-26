#!/usr/bin/env bash
# Reset the users-service database — deletes all data.
# Usage: ./scripts/reset-db.sh
#
# Connection: uses PGPASSWORD env var or defaults to 'medical123'.
# Requires: psql client with access to the users_db database via localhost.

set -euo pipefail

DB_NAME="${DB_NAME:-users_db}"
DB_USER="${DB_USER:-medical_user}"
DB_HOST="${DB_HOST:-localhost}"
export PGPASSWORD="${DB_PASSWORD:-medical123}"

echo "🔄 Resetting database: $DB_NAME"
echo "⚠️  This will DELETE ALL DATA in the database."

psql -h "$DB_HOST" -U "$DB_USER" -d "$DB_NAME" <<SQL
-- Delete all rows (respecting FK order)
DELETE FROM professionals;
DELETE FROM patients;
DELETE FROM users;
SQL

echo "✅ Database reset complete. All tables are empty."
