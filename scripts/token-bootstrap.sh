#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
ENV_FILE="${REPO_ROOT}/.env.dev"

BASE_URL="${VIDEOMINER_BASE_URL:-http://localhost:8080/videoMiner/v1}"
MANAGEMENT_KEY="${VIDEOMINER_TOKEN_MANAGEMENT_KEY:-${VIDEOMINER_MANAGEMENT_KEY:-}}"
TOKEN_TTL_HOURS="${TOKEN_TTL_HOURS:-24}"
PROTECTED_PATH="${PROTECTED_PATH:-/channels}"

require_command() {
  local command_name="$1"
  if ! command -v "${command_name}" >/dev/null 2>&1; then
    echo "ERROR: Required command '${command_name}' is not available in PATH." >&2
    exit 1
  fi
}

require_command "curl"
require_command "python3"

if [[ -z "${MANAGEMENT_KEY}" && -f "${ENV_FILE}" ]]; then
  set -a
  # shellcheck disable=SC1091
  source "${ENV_FILE}"
  set +a
  MANAGEMENT_KEY="${VIDEOMINER_TOKEN_MANAGEMENT_KEY:-${VIDEOMINER_MANAGEMENT_KEY:-}}"
fi

if [[ -z "${MANAGEMENT_KEY}" ]]; then
  echo "ERROR: Set VIDEOMINER_TOKEN_MANAGEMENT_KEY (or VIDEOMINER_MANAGEMENT_KEY)." >&2
  exit 1
fi

tmp_issue_1="$(mktemp)"
tmp_issue_2="$(mktemp)"
trap 'rm -f "${tmp_issue_1}" "${tmp_issue_2}"' EXIT

json_get() {
  local file="$1"
  local key="$2"
  python3 - "$file" "$key" <<'PY'
import json
import pathlib
import sys

doc = json.loads(pathlib.Path(sys.argv[1]).read_text(encoding="utf-8"))
print(doc.get(sys.argv[2], "") or "")
PY
}

issue_token() {
  local output_file="$1"
  curl --fail --silent --show-error \
    -X POST "${BASE_URL}/token" \
    -H "Content-Type: application/json" \
    -H "X-Token-Management-Key: ${MANAGEMENT_KEY}" \
    -d "{\"ttlHours\": ${TOKEN_TTL_HOURS}}" \
    -o "${output_file}"
}

call_protected() {
  local token="$1"
  curl --fail --silent --show-error \
    -X GET "${BASE_URL}${PROTECTED_PATH}" \
    -H "Authorization: Bearer ${token}" >/dev/null
}

revoke_token() {
  local token_id="$1"
  curl --fail --silent --show-error \
    -X DELETE "${BASE_URL}/token/${token_id}" \
    -H "X-Token-Management-Key: ${MANAGEMENT_KEY}" >/dev/null
}

echo "1) Emitiendo token inicial..."
issue_token "${tmp_issue_1}"
old_token_id="$(json_get "${tmp_issue_1}" "tokenId")"
old_access_token="$(json_get "${tmp_issue_1}" "accessToken")"

if [[ -z "${old_token_id}" || -z "${old_access_token}" ]]; then
  echo "ERROR: respuesta invalida al emitir token:" >&2
  cat "${tmp_issue_1}" >&2
  exit 1
fi

echo "2) Probando endpoint protegido..."
call_protected "${old_access_token}"

echo "3) Rotando token..."
issue_token "${tmp_issue_2}"
new_token_id="$(json_get "${tmp_issue_2}" "tokenId")"
new_access_token="$(json_get "${tmp_issue_2}" "accessToken")"

if [[ -z "${new_token_id}" || -z "${new_access_token}" ]]; then
  echo "ERROR: respuesta invalida al emitir nuevo token:" >&2
  cat "${tmp_issue_2}" >&2
  exit 1
fi

call_protected "${new_access_token}"
revoke_token "${old_token_id}"

echo
echo "Listo. Usa este token:"
echo "export VIDEOMINER_TOKEN_ID='${new_token_id}'"
echo "export VIDEOMINER_TOKEN='Bearer ${new_access_token}'"
