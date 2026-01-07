# 🚀 How to Run Reposcribe Locally

## Prerequisites

1. **Java 17+** - Check with: `java -version`
2. **Maven 3.6+** - Check with: `mvn -version`
3. **Node.js 18+** - Check with: `node -v`
4. **npm** - Check with: `npm -v`
5. **Ollama** (for AI features) - Download from https://ollama.ai

## Step-by-Step Setup

### Step 1: Install Ollama (Required for AI Features)

```bash
# Download and install Ollama from https://ollama.ai
# Or on macOS:
brew install ollama

# Start Ollama server (in a separate terminal)
ollama serve

# In another terminal, pull the model
ollama pull llama3
```

**Note**: If you don't have Ollama, the app will still work but AI features (documentation generation) won't function.

### Step 2: Start the Backend

```bash
# Navigate to backend directory
cd /Users/dakshitha.k/Reposcribe/backend

# Make sure dependencies are installed
mvn clean install

# Start the Spring Boot application
mvn spring-boot:run
```

The backend will start on **http://localhost:8080**

You should see:
```
Started BackendApplication in X.XXX seconds
```

### Step 3: Start the Frontend (in a new terminal)

```bash
# Navigate to frontend directory
cd /Users/dakshitha.k/Reposcribe/frontend

# Install dependencies (if not already done)
npm install

# Start the development server
npm run dev
```

The frontend will start on **http://localhost:5173** (or another port if 5173 is busy)

### Step 4: Access the Application

Open your browser and go to: **http://localhost:5173**

## Testing the Complete Workflow

### 1. Register a New User
- Click "Register" or go to `/register`
- Enter username and password
- Click "Register"
- You'll be automatically logged in

### 2. Upload a Repository

**Option A: Upload ZIP File**
- Go to Dashboard
- Click "File Upload" tab
- Select a ZIP file containing code (Java, Python, or JavaScript)
- Click "Upload"
- Wait for upload to complete

**Option B: Clone Git Repository**
- Go to Dashboard
- Click "Git Clone" tab
- Enter repository URL (e.g., `https://github.com/user/repo.git`)
- For private repos, enter username and password
- Click "Clone"
- Wait for clone to complete

### 3. Generate Documentation
- After upload/clone, you'll be redirected to Documentation Viewer
- Documentation will start generating automatically
- You'll see progress updates
- Once complete, you can:
  - View the generated README.md
  - Download it as a file

### 4. Test API Endpoints (Optional)

You can test the API directly using curl:

```bash
# Get JWT token (after login)
TOKEN="your-jwt-token-here"

# Health check
curl http://localhost:8080/api/health

# Check AI service
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/analysis/health

# Generate documentation
curl -X POST \
  -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/documentation/generate?sessionId=YOUR_SESSION_ID"
```

## Troubleshooting

### Backend Issues

**Port 8080 already in use:**
```bash
# Change port in application.properties
# Or kill the process using port 8080
lsof -ti:8080 | xargs kill -9
```

**Maven build fails:**
```bash
# Clean and rebuild
mvn clean install
```

**Ollama connection errors:**
- Make sure Ollama is running: `ollama serve`
- Check if model is downloaded: `ollama list`
- Verify Ollama URL in `application.properties` (default: http://localhost:11434)

### Frontend Issues

**Port 5173 already in use:**
- Vite will automatically use the next available port
- Check the terminal output for the actual port

**npm install fails:**
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

**Build errors:**
```bash
# Check TypeScript errors
npm run build
```

### Common Issues

**"AI service is not available" error:**
- Start Ollama: `ollama serve`
- Pull model: `ollama pull llama3`
- Check backend logs for connection errors

**"Invalid session ID" error:**
- Make sure you've uploaded/cloned a repository first
- Check that sessionId is stored in browser localStorage

**CORS errors:**
- Backend is configured to allow requests from `http://localhost:5173`
- If using a different port, update `application.properties`:
  ```
  cors.allowed-origins=http://localhost:5173,http://localhost:YOUR_PORT
  ```

## Quick Start Script

You can create a script to start everything:

```bash
#!/bin/bash
# start-reposcribe.sh

echo "Starting Reposcribe..."

# Start Ollama (if not running)
if ! pgrep -x "ollama" > /dev/null; then
    echo "Starting Ollama..."
    ollama serve &
    sleep 2
    ollama pull llama3 &
fi

# Start backend
echo "Starting backend..."
cd backend
mvn spring-boot:run &
BACKEND_PID=$!

# Wait for backend to start
sleep 10

# Start frontend
echo "Starting frontend..."
cd ../frontend
npm run dev &
FRONTEND_PID=$!

echo "Backend PID: $BACKEND_PID"
echo "Frontend PID: $FRONTEND_PID"
echo "Backend: http://localhost:8080"
echo "Frontend: http://localhost:5173"
echo ""
echo "Press Ctrl+C to stop all services"
```

## Environment Variables (Optional)

You can customize configuration via environment variables:

**Backend:**
- `SPRING_PROFILES_ACTIVE=dev`
- `SERVER_PORT=8080`
- `OLLAMA_BASE_URL=http://localhost:11434`
- `OLLAMA_MODEL=llama3`

**Frontend:**
- `VITE_API_URL=http://localhost:8080/api`

## Next Steps

1. ✅ Test with a simple Java project
2. ✅ Test with a Python project
3. ✅ Test with a JavaScript/React project
4. ✅ Try different repository sizes
5. ✅ Test error handling (invalid files, network errors)

## Support

If you encounter issues:
1. Check the logs in both backend and frontend terminals
2. Verify all prerequisites are installed
3. Check that ports 8080 and 5173 are available
4. Ensure Ollama is running for AI features

Happy testing! 🎉

