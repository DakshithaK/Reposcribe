#!/bin/bash

# Reposcribe Quick Start Script

echo "🚀 Starting Reposcribe..."
echo ""

# Check if Ollama is installed
if ! command -v ollama &> /dev/null; then
    echo "⚠️  Ollama is not installed. AI features won't work."
    echo "   Install from: https://ollama.ai"
    echo "   Or on macOS: brew install ollama"
    echo ""
    read -p "Continue without Ollama? (y/n) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    # Check if Ollama is running
    if ! pgrep -x "ollama" > /dev/null; then
        echo "📦 Starting Ollama server..."
        ollama serve > /dev/null 2>&1 &
        OLLAMA_PID=$!
        sleep 3
        
        # Check if model is available
        if ! ollama list | grep -q "llama3"; then
            echo "📥 Downloading llama3 model (this may take a while)..."
            ollama pull llama3
        fi
        echo "✅ Ollama is ready"
    else
        echo "✅ Ollama is already running"
    fi
fi

echo ""
echo "🔧 Starting Backend..."
cd "$(dirname "$0")/backend"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

# Start backend in background
mvn spring-boot:run > ../backend.log 2>&1 &
BACKEND_PID=$!

echo "   Backend starting... (PID: $BACKEND_PID)"
echo "   Logs: backend.log"
echo "   Waiting for backend to be ready..."

# Wait for backend to start (check health endpoint)
for i in {1..30}; do
    sleep 2
    if curl -s http://localhost:8080/api/health > /dev/null 2>&1; then
        echo "✅ Backend is ready at http://localhost:8080"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "⚠️  Backend is taking longer than expected. Check backend.log"
    fi
done

echo ""
echo "🎨 Starting Frontend..."
cd "../frontend"

# Check if Node is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js first."
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo "   Installing dependencies..."
    npm install
fi

# Start frontend
npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!

echo "   Frontend starting... (PID: $FRONTEND_PID)"
echo "   Logs: frontend.log"
sleep 3

echo ""
echo "✨ Reposcribe is starting!"
echo ""
echo "📍 URLs:"
echo "   Frontend: http://localhost:5173"
echo "   Backend:  http://localhost:8080"
echo ""
echo "📝 Logs:"
echo "   Backend:  tail -f backend.log"
echo "   Frontend: tail -f frontend.log"
echo ""
echo "🛑 To stop all services:"
echo "   kill $BACKEND_PID $FRONTEND_PID $OLLAMA_PID 2>/dev/null"
echo ""
echo "Press Ctrl+C to stop (or run: ./stop.sh)"

# Save PIDs to file for stop script
echo "$BACKEND_PID $FRONTEND_PID $OLLAMA_PID" > .pids

# Wait for user interrupt
trap "echo ''; echo 'Stopping services...'; kill $BACKEND_PID $FRONTEND_PID $OLLAMA_PID 2>/dev/null; rm -f .pids; exit" INT
wait

