#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

ENV_FILE=".env.dev"
DETACH="true"
DRY_RUN="false"

declare -a EXPLICIT_SERVICES=()
declare -a TARGET_SERVICES=()
declare -a CHANGED_FILES=()

usage() {
  cat <<'EOF'
Usage: scripts/dev-recreate-changed.sh [--service <name>]... [--attach] [--dry-run]

Recrea solo los microservicios modificados en desarrollo.
No recrea dependencias automáticamente.

Sin --service, detecta cambios con git en:
- archivos modificados sin commitear
- cambios staged
- archivos nuevos sin seguimiento

Servicios válidos:
- videominer
- vimeominer
- youtubeminer

Options:
  --service <name>  Fuerza recreación de un servicio concreto (se puede repetir).
  --attach          Ejecuta en primer plano (sin -d).
  --dry-run         Muestra servicios detectados y comando final, sin ejecutar nada.
EOF
}

contains_service() {
  local service="$1"
  shift
  local item
  for item in "$@"; do
    if [[ "$item" == "$service" ]]; then
      return 0
    fi
  done
  return 1
}

add_target_service() {
  local service="$1"
  if ! contains_service "$service" "${TARGET_SERVICES[@]}"; then
    TARGET_SERVICES+=("$service")
  fi
}

add_all_java_services() {
  add_target_service "videominer"
  add_target_service "vimeominer"
  add_target_service "youtubeminer"
}

for ((i = 1; i <= $#; i++)); do
  arg="${!i}"
  case "$arg" in
    --service)
      next_index=$((i + 1))
      if [[ $next_index -gt $# ]]; then
        echo "Missing value for --service"
        usage
        exit 1
      fi
      service_name="${!next_index}"
      case "$service_name" in
        videominer|vimeominer|youtubeminer)
          EXPLICIT_SERVICES+=("$service_name")
          ;;
        *)
          echo "Invalid service: $service_name"
          usage
          exit 1
          ;;
      esac
      i=$next_index
      ;;
    --attach)
      DETACH="false"
      ;;
    --dry-run)
      DRY_RUN="true"
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

if [[ "${#EXPLICIT_SERVICES[@]}" -gt 0 ]]; then
  for service in "${EXPLICIT_SERVICES[@]}"; do
    add_target_service "$service"
  done
else
  mapfile -t CHANGED_FILES < <(
    {
      git diff --name-only
      git diff --name-only --cached
      git ls-files --others --exclude-standard
    } | awk 'NF' | sort -u
  )

  for file_path in "${CHANGED_FILES[@]}"; do
    case "$file_path" in
      VideoMiner/*)
        add_target_service "videominer"
        ;;
      VimeoMiner/*)
        add_target_service "vimeominer"
        ;;
      YoutubeMiner/*)
        add_target_service "youtubeminer"
        ;;
      pom.xml|docker-compose.yml|docker-compose.dev.yml|docker-compose.prod.yml|scripts/dev-java-runner.sh|.env.dev|.env.dev.example)
        add_all_java_services
        ;;
    esac
  done
fi

if [[ "${#TARGET_SERVICES[@]}" -eq 0 ]]; then
  echo "[dev-recreate-changed] No Java microservice changes detected."
  exit 0
fi

BASE_CMD=(
  docker compose
  --env-file "${ENV_FILE}"
  -f docker-compose.yml
  -f docker-compose.dev.yml
)

declare -a REQUIRED_RUNNING=()
if contains_service "videominer" "${TARGET_SERVICES[@]}"; then
  REQUIRED_RUNNING+=("postgres")
fi
if contains_service "vimeominer" "${TARGET_SERVICES[@]}" || contains_service "youtubeminer" "${TARGET_SERVICES[@]}"; then
  if ! contains_service "videominer" "${REQUIRED_RUNNING[@]}"; then
    REQUIRED_RUNNING+=("videominer")
  fi
fi

mapfile -t RUNNING_SERVICES < <("${BASE_CMD[@]}" ps --services --status running)
declare -a MISSING_RUNNING=()
for required_service in "${REQUIRED_RUNNING[@]}"; do
  if ! contains_service "${required_service}" "${RUNNING_SERVICES[@]}"; then
    MISSING_RUNNING+=("${required_service}")
  fi
done

if [[ "${DRY_RUN}" == "true" ]]; then
  echo "[dev-recreate-changed] Target services: ${TARGET_SERVICES[*]}"
  if [[ "${#REQUIRED_RUNNING[@]}" -gt 0 ]]; then
    echo "[dev-recreate-changed] Required running services: ${REQUIRED_RUNNING[*]}"
  fi
  if [[ "${#MISSING_RUNNING[@]}" -gt 0 ]]; then
    echo "[dev-recreate-changed] Missing running services: ${MISSING_RUNNING[*]}"
  fi
  echo "[dev-recreate-changed] Dry run enabled."
  exit 0
fi

if [[ "${#MISSING_RUNNING[@]}" -gt 0 ]]; then
  echo "[dev-recreate-changed] Missing required running services: ${MISSING_RUNNING[*]}"
  echo "[dev-recreate-changed] Start them first, for example:"
  echo "  docker compose --env-file ${ENV_FILE} -f docker-compose.yml -f docker-compose.dev.yml up -d ${MISSING_RUNNING[*]}"
  exit 1
fi

UP_CMD=(
  "${BASE_CMD[@]}"
  up
  --build
  --force-recreate
  --remove-orphans
  --no-deps
)

if [[ "${DETACH}" == "true" ]]; then
  UP_CMD+=(-d)
fi

UP_CMD+=("${TARGET_SERVICES[@]}")

echo "[dev-recreate-changed] Recreating: ${TARGET_SERVICES[*]}"
"${UP_CMD[@]}"

echo "[dev-recreate-changed] Done."
