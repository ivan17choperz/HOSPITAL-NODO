-- Creates a replication role used by streaming replicas to connect to the primary.
-- This script runs automatically on first startup via the postgres Docker image init hook.

CREATE ROLE replicator WITH REPLICATION LOGIN PASSWORD 'replicator_pass';
