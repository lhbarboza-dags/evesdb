#!/usr/bin/env bash
# Builds the backend jar and the frontend bundle on the host, so the Docker images only copy finished artifacts.
# (Use this when Docker builds cannot reach Maven Central / npm, e.g. no outbound network inside containers.)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

echo "==> backend: mvn package"
(cd "$ROOT/backend" && mvn -B -q -DskipTests package)
test -f "$ROOT/backend/target/sibol-eves-api.jar"

echo "==> frontend: npm ci && npm run build"
(cd "$ROOT/frontend" && npm ci --no-audit --no-fund && npm run build)
test -f "$ROOT/frontend/dist/index.html"

echo
echo "Built backend/target/sibol-eves-api.jar and frontend/dist/."
echo "Next: docker compose up --build"
