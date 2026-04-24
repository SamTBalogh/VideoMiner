#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

ENV_FILE=".env.dev"
DROP_DB="false"
DETACH="true"

usage() {
  cat <<'EOF'
Usage: scripts/dev-recreate-clean.sh [--drop-db] [--attach]

Recrea el entorno de desarrollo de forma limpia:
1) down --remove-orphans
2) up --build --force-recreate --remove-orphans

Options:
  --drop-db   También elimina volúmenes (equivalente a down -v).
  --attach    Ejecuta en primer plano (sin -d).
EOF
}

for arg in "$@"; do
  case "$arg" in
    --drop-db)
      DROP_DB="true"
      ;;
    --attach)
      DETACH="false"
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $arg"
      usage
      exit 1
      ;;
  esac
done

cd "${REPO_ROOT}"

DOWN_CMD=(
  docker compose
  --env-file "${ENV_FILE}"
  -f docker-compose.yml
  -f docker-compose.dev.yml
  down
  --remove-orphans
)

if [[ "${DROP_DB}" == "true" ]]; then
  DOWN_CMD+=(-v)
fi

UP_CMD=(
  docker compose
  --env-file "${ENV_FILE}"
  -f docker-compose.yml
  -f docker-compose.dev.yml
  up
  --build
  --force-recreate
  --remove-orphans
)

if [[ "${DETACH}" == "true" ]]; then
  UP_CMD+=(-d)
fi

echo "[dev-clean] Stopping current dev stack..."
"${DOWN_CMD[@]}"

echo "[dev-clean] Recreating dev stack..."
"${UP_CMD[@]}"

echo "[dev-clean] Done."
