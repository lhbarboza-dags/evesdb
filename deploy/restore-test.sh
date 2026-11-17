#!/usr/bin/env bash
# Restores the newest (or given) dump into a throwaway Postgres container and prints row counts to compare with prod.
# Weekly cron:  0 4 * * 0  /opt/sibol-eves/SIBOL-EVES-NEXT/deploy/restore-test.sh >> /var/log/sibol-backup.log 2>&1
set -euo pipefail
BACKUP_DIR=${BACKUP_DIR:-/opt/sibol-eves/backups}
DUMP=${1:-$(ls -1t "$BACKUP_DIR"/evesdb-*.dump | head -1)}
NAME=sibol-restore-test
docker rm -f "$NAME" >/dev/null 2>&1 || true
docker run -d --name "$NAME" -e POSTGRES_PASSWORD=test -e POSTGRES_DB=evesdb postgres:17-alpine >/dev/null
trap 'docker rm -f "$NAME" >/dev/null 2>&1' EXIT
until docker exec "$NAME" pg_isready -U postgres -d evesdb >/dev/null 2>&1; do sleep 2; done
docker exec -i "$NAME" pg_restore -U postgres -d evesdb --no-owner --no-acl < "$DUMP"
echo "$(date -u) restore test of $(basename "$DUMP"):"
docker exec "$NAME" psql -U postgres -d evesdb -Atc \
  "select 'studysrc', count(*) from studysrc union all select 'site', count(*) from site union all select 'dataset', count(*) from dataset union all select 'user', count(*) from \"user\""
