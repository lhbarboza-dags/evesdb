#!/usr/bin/env bash
# docker compose wrapper for the production stack: ./prod.sh up -d --build | ps | logs -f backend | down
set -euo pipefail
cd "$(dirname "$0")"
ENV_FILE=../.env.prod
[ -f "$ENV_FILE" ] || { echo "Missing $ENV_FILE (copy ../.env.prod.example and fill it in)"; exit 1; }
FILES=(-f compose.prod.yml)
# optional extra compose file, e.g. for a local dress rehearsal: COMPOSE_OVERRIDE=/path/override.yml ./prod.sh up -d
[ -n "${COMPOSE_OVERRIDE:-}" ] && FILES+=(-f "$COMPOSE_OVERRIDE")
exec docker compose --env-file "$ENV_FILE" "${FILES[@]}" "$@"
