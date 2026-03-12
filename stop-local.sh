#!/usr/bin/env bash
set -euo pipefail

SERVER_PORT=8080
WEB_PORT=3001

stop_by_port() {
  local port="$1"
  local name="$2"
  local pids

  pids="$(lsof -tiTCP:"$port" -sTCP:LISTEN || true)"
  if [ -n "$pids" ]; then
    printf 'Stopping %s on port %s (PID: %s)\n' "$name" "$port" "$pids"
    kill $pids
  else
    printf '%s not running on port %s.\n' "$name" "$port"
  fi
}

if ! command -v lsof >/dev/null 2>&1; then
  printf 'Error: lsof is required but not found.\n'
  exit 1
fi

stop_by_port "$WEB_PORT" "Frontend"
stop_by_port "$SERVER_PORT" "Backend"

if command -v brew >/dev/null 2>&1; then
  printf 'Stopping MySQL service (Homebrew)...\n'
  brew services stop mysql >/dev/null || true
fi

printf 'Done.\n'
