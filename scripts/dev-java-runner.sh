#!/usr/bin/env bash

set -euo pipefail

MODULE_NAME="${1:-}"
WATCH_INTERVAL_SECONDS="${DEV_WATCH_INTERVAL_SECONDS:-2}"

if [[ -z "${MODULE_NAME}" ]]; then
  echo "[dev-runner] Usage: dev-java-runner.sh <MAVEN_MODULE_NAME>"
  exit 1
fi

MODULE_SRC_DIR="${MODULE_NAME}/src/main"
MODULE_POM="${MODULE_NAME}/pom.xml"
ROOT_POM="pom.xml"

compile_module() {
  mvn -B -ntp -f "${MODULE_POM}" -DskipTests compile
}

snapshot_hash() {
  {
    if [[ -d "${MODULE_SRC_DIR}" ]]; then
      find "${MODULE_SRC_DIR}" -type f -print0
    fi
    printf '%s\0' "${MODULE_POM}" "${ROOT_POM}"
  } | xargs -0 -r stat -c '%n:%Y' 2>/dev/null | sort | sha1sum | awk '{print $1}'
}

echo "[dev-runner] Initial compile for module ${MODULE_NAME}..."
compile_module

LAST_SNAPSHOT="$(snapshot_hash)"

watch_and_compile() {
  while true; do
    sleep "${WATCH_INTERVAL_SECONDS}"
    CURRENT_SNAPSHOT="$(snapshot_hash)"

    if [[ "${CURRENT_SNAPSHOT}" != "${LAST_SNAPSHOT}" ]]; then
      echo "[dev-runner] Change detected for ${MODULE_NAME}. Recompiling..."
      if compile_module; then
        LAST_SNAPSHOT="${CURRENT_SNAPSHOT}"
        echo "[dev-runner] Compile OK for ${MODULE_NAME}."
      else
        echo "[dev-runner] Compile failed for ${MODULE_NAME}. Waiting for next change."
      fi
    fi
  done
}

watch_and_compile &
WATCH_PID=$!

cleanup() {
  kill "${WATCH_PID}" 2>/dev/null || true
}

trap cleanup EXIT INT TERM

echo "[dev-runner] Starting Spring Boot for ${MODULE_NAME}..."
mvn -B -ntp -f "${MODULE_POM}" spring-boot:run -Dspring-boot.run.addResources=true
