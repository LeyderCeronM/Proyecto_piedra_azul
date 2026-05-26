#!/usr/bin/env bash
# Stop the users-service.
# Usage: ./scripts/stop.sh

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PID_FILE="$SCRIPT_DIR/.service.pid"

if [ ! -f "$PID_FILE" ]; then
    echo "[stop] No PID file found. Is the service running?"
    # Try to find any Java process on port 8081 as fallback
    JAVA_PID=$(ss -tlnp 2>/dev/null | grep ":8081 " | grep -oP 'pid=\K[0-9]+' | head -1)
    if [ -n "$JAVA_PID" ]; then
        echo "[stop] Found Java process on port 8081 (PID $JAVA_PID). Killing..."
        kill "$JAVA_PID" 2>/dev/null && echo "[stop] Stopped." || echo "[stop] Could not kill process."
    else
        echo "[stop] No process found on port 8081. Nothing to stop."
    fi
    exit 0
fi

PID=$(cat "$PID_FILE")

if ! kill -0 "$PID" 2>/dev/null; then
    echo "[stop] Process $PID is not running. Cleaning up."
    rm -f "$PID_FILE"
    exit 0
fi

echo "[stop] Stopping users-service (PID $PID)..."
kill "$PID" 2>/dev/null || true

# Give it time to shut down gracefully
TIMEOUT=15
ELAPSED=0
while [ $ELAPSED -lt $TIMEOUT ]; do
    if ! kill -0 "$PID" 2>/dev/null; then
        echo "[stop] Service stopped gracefully."
        rm -f "$PID_FILE"
        exit 0
    fi
    sleep 1
    ELAPSED=$((ELAPSED + 1))
done

# Force kill if still running
echo "[stop] Force killing (PID $PID)..."
kill -9 "$PID" 2>/dev/null || true
rm -f "$PID_FILE"
echo "[stop] Service force-stopped."
