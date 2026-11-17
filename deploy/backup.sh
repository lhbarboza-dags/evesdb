#!/usr/bin/env bash
# Nightly PostgreSQL backup: custom-format dump → local folder (14 days) → optional upload to OCI Object Storage.
# cron (as the deploy user):  30 2 * * *  /opt/sibol-eves/SIBOL-EVES-NEXT/deploy/backup.sh >> /var/log/sibol-backup.log 2>&1
# Upload: set OCI_BUCKET (and install/configure the OCI CLI, or use an instance principal: OCI_CLI_AUTH=instance_principal).
set -euo pipefail
cd "$(dirname "$0")"
BACKUP_DIR=${BACKUP_DIR:-/opt/sibol-eves/backups}
KEEP_DAYS=${KEEP_DAYS:-14}
if [ -z "${OCI_BUCKET:-}" ] && [ -f ../.env.prod ]; then
  OCI_BUCKET=$(grep -E '^OCI_BUCKET=' ../.env.prod | cut -d= -f2- || true)
fi
mkdir -p "$BACKUP_DIR"
FILE="$BACKUP_DIR/evesdb-$(date -u +%Y%m%dT%H%M%SZ).dump"

./prod.sh exec -T db sh -c 'pg_dump -U "$POSTGRES_USER" -d evesdb -Fc --no-owner --no-acl' > "$FILE"
[ -s "$FILE" ] || { echo "$(date -u) backup FAILED: empty dump"; rm -f "$FILE"; exit 1; }
echo "$(date -u) backup ok: $FILE ($(du -h "$FILE" | cut -f1))"

if [ -n "${OCI_BUCKET:-}" ] && ! command -v oci >/dev/null 2>&1; then
  echo "$(date -u) WARNING: OCI_BUCKET is set but the OCI CLI is not installed; kept the local copy only"
elif [ -n "${OCI_BUCKET:-}" ]; then
  oci os object put --bucket-name "$OCI_BUCKET" --file "$FILE" --name "db/$(basename "$FILE")" --force >/dev/null
  echo "$(date -u) uploaded to oci://$OCI_BUCKET/db/$(basename "$FILE")"
fi

find "$BACKUP_DIR" -name 'evesdb-*.dump' -mtime +"$KEEP_DAYS" -delete
