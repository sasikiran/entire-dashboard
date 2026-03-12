#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="$ROOT_DIR/server"
WEB_DIR="$ROOT_DIR/web"

SERVER_PORT=8080
WEB_PORT=3001

SERVER_LOG="/tmp/entire-server.log"
WEB_LOG="/tmp/entire-web.log"

if ! command -v lsof >/dev/null 2>&1; then
  printf 'Error: lsof is required but not found.\n'
  exit 1
fi

if ! command -v brew >/dev/null 2>&1; then
  printf 'Error: Homebrew is required for managing local MySQL service.\n'
  exit 1
fi

java_ready() {
  command -v java >/dev/null 2>&1 && java -version >/dev/null 2>&1
}

if ! java_ready; then
  if [ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    # shellcheck disable=SC1090
    set +u
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    set -u
  fi
fi

if ! java_ready; then
  BREW_JAVA_HOME="$(brew --prefix openjdk@25 2>/dev/null || brew --prefix openjdk 2>/dev/null || true)"
  if [ -n "$BREW_JAVA_HOME" ] && [ -x "$BREW_JAVA_HOME/bin/java" ]; then
    export JAVA_HOME="$BREW_JAVA_HOME"
    export PATH="$JAVA_HOME/bin:$PATH"
  fi
fi

if ! java_ready; then
  printf 'Error: Java runtime is not available.\n'
  printf 'Install Java 25 and try again.\n'
  exit 1
fi

if [ ! -f "$SERVER_DIR/.env" ] && [ -f "$SERVER_DIR/.env.dist" ]; then
  cp "$SERVER_DIR/.env.dist" "$SERVER_DIR/.env"
  printf 'Created server/.env from template. Update DB credentials if needed.\n'
fi

printf 'Starting MySQL service (Homebrew)...\n'
brew services start mysql >/dev/null

if lsof -nP -iTCP:"$SERVER_PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  printf 'Backend already running on port %s.\n' "$SERVER_PORT"
else
  printf 'Starting backend on port %s...\n' "$SERVER_PORT"
  (
    cd "$SERVER_DIR"
    nohup ./gradlew bootRun >"$SERVER_LOG" 2>&1 &
  )
fi

if lsof -nP -iTCP:"$WEB_PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  WEB_PID="$(lsof -tiTCP:"$WEB_PORT" -sTCP:LISTEN | head -n 1)"
  WEB_CMD="$(ps -p "$WEB_PID" -o command= 2>/dev/null || true)"
  printf 'Frontend already running on port %s (PID %s).\n' "$WEB_PORT" "$WEB_PID"
  if [ -n "$WEB_CMD" ]; then
    printf 'Existing process: %s\n' "$WEB_CMD"
  fi
else
  printf 'Starting frontend on port %s...\n' "$WEB_PORT"
  (
    cd "$WEB_DIR"
    nohup pnpm dev --host 0.0.0.0 --port "$WEB_PORT" >"$WEB_LOG" 2>&1 &
  )
fi

printf 'Waiting for services to bind ports...\n'
sleep 4

printf '\nLocal services status:\n'
if lsof -nP -iTCP:"$SERVER_PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  SERVER_PID="$(lsof -tiTCP:"$SERVER_PORT" -sTCP:LISTEN | head -n 1)"
  printf '  Backend : http://127.0.0.1:%s (PID %s)\n' "$SERVER_PORT" "$SERVER_PID"
else
  printf '  Backend : not running (check %s)\n' "$SERVER_LOG"
fi

if lsof -nP -iTCP:"$WEB_PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  WEB_PID="$(lsof -tiTCP:"$WEB_PORT" -sTCP:LISTEN | head -n 1)"
  printf '  Frontend: http://127.0.0.1:%s (PID %s)\n' "$WEB_PORT" "$WEB_PID"
else
  printf '  Frontend: not running (check %s)\n' "$WEB_LOG"
fi

printf '\nLogs:\n'
printf '  %s\n' "$SERVER_LOG"
printf '  %s\n' "$WEB_LOG"
