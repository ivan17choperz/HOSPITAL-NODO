#!/bin/bash
# Initializes a PostgreSQL streaming replica from a primary using pg_basebackup.
# Runs once on container start; if data directory is already populated it exits early.

set -e

PGDATA="${PGDATA:-/var/lib/postgresql/data}"
PRIMARY_HOST="${PRIMARY_HOST:-postgres-node1}"
PRIMARY_PORT="${PRIMARY_PORT:-5432}"
REPL_USER="${REPL_USER:-replicator}"
REPL_PASSWORD="${REPL_PASSWORD:-replicator_pass}"

if [ -f "$PGDATA/PG_VERSION" ]; then
  echo "[REPLICA] Data directory already initialized — skipping pg_basebackup."
  exec postgres
fi

echo "[REPLICA] Waiting for primary at $PRIMARY_HOST:$PRIMARY_PORT..."
until pg_isready -h "$PRIMARY_HOST" -p "$PRIMARY_PORT" -U "$REPL_USER" 2>/dev/null; do
  sleep 2
done

echo "[REPLICA] Running pg_basebackup from $PRIMARY_HOST..."
PGPASSWORD="$REPL_PASSWORD" pg_basebackup \
  -h "$PRIMARY_HOST" \
  -p "$PRIMARY_PORT" \
  -U "$REPL_USER" \
  -D "$PGDATA" \
  -Fp -Xs -P -R

echo "[REPLICA] Base backup complete. Starting replica..."
exec postgres
