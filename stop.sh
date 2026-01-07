#!/bin/bash

# Stop Reposcribe services

echo "🛑 Stopping Reposcribe services..."

if [ -f .pids ]; then
    PIDS=$(cat .pids)
    for PID in $PIDS; do
        if kill -0 $PID 2>/dev/null; then
            kill $PID 2>/dev/null
            echo "   Stopped process $PID"
        fi
    done
    rm -f .pids
fi

# Also try to kill by process name
pkill -f "spring-boot:run" 2>/dev/null
pkill -f "vite" 2>/dev/null

echo "✅ All services stopped"

