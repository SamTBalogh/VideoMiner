#!/bin/sh

set -eu

cd /workspace/frontend

LOCK_HASH_FILE="node_modules/.lockfile-hash"
CURRENT_HASH="$(sha256sum package-lock.json | awk '{print $1}')"
STORED_HASH=""

if [ -f "$LOCK_HASH_FILE" ]; then
  STORED_HASH="$(cat "$LOCK_HASH_FILE")"
fi

if [ ! -d node_modules ] || [ -z "$(ls -A node_modules 2>/dev/null)" ] || [ "$CURRENT_HASH" != "$STORED_HASH" ]; then
  echo "Installing frontend dependencies with npm ci..."
  npm ci
  mkdir -p node_modules
  echo "$CURRENT_HASH" > "$LOCK_HASH_FILE"
fi

exec npm run dev -- --host 0.0.0.0 --port 5173
