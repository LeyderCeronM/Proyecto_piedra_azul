#!/usr/bin/env bash
# Start the users-service in background.
# Usage: ./scripts/start.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
LOG_DIR="$PROJECT_DIR/logs"
PID_FILE="$SCRIPT_DIR/.service.pid"

# Ensure logs directory exists
mkdir -p "$LOG_DIR"

# Check if already running
if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
    echo "[start] Service already running (PID $(cat "$PID_FILE")). Use stop.sh first."
    exit 1
fi

# Clean old PID file if process is dead
rm -f "$PID_FILE"

echo "[start] Building and starting users-service..."
echo "[start] Logs: $LOG_DIR/app.log"

cd "$PROJECT_DIR"

# Start the service in background.
# mvn spring-boot:run runs in the foreground, so we wrap it with nohup.
nohup mvn spring-boot:run -q > "$LOG_DIR/app.log" 2>&1 &
SHELL_PID=$!

# Give Maven time to spawn the Spring Boot process.
# We wait until the port opens or timeout.
echo "[start] Waiting for server to start on port 8081..."
TIMEOUT=60
ELAPSED=0
while [ $ELAPSED -lt $TIMEOUT ]; do
    if ss -tlnp 2>/dev/null | grep -q ":8081 "; then
        # Find the actual Java process that owns the port
        JAVA_PID=$(ss -tlnp 2>/dev/null | grep ":8081 " | grep -oP 'pid=\K[0-9]+' | head -1)
        if [ -n "$JAVA_PID" ]; then
            echo "$JAVA_PID" > "$PID_FILE"
            echo "[start] users-service started (PID $JAVA_PID)"
            echo "[start] Ready at http://localhost:8081"
            exit 0
        fi
    fi
    # Check if the shell process died
    if ! kill -0 "$SHELL_PID" 2>/dev/null; then
        echo "[start] ERROR: Maven process exited prematurely. Check logs."
        exit 1
    fi
    sleep 1
    ELAPSED=$((ELAPSED + 1))
done

echo "[start] ERROR: Timed out waiting for server to start. Check logs."
exit 1
