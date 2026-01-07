# Reposcribe

AI-powered documentation generator for code repositories. Upload your code or clone from Git, and generate comprehensive documentation automatically.

## Features

- ✅ **No Code Storage** - Privacy-first approach, all code processed in-memory
- ✅ **Local AI Processing** - Uses Ollama for local AI processing (no external APIs)
- ✅ **In-Memory Sessions** - No database required
- ✅ **Multiple Input Methods** - Upload ZIP files or clone from Git repositories
- ✅ **JWT Authentication** - Secure token-based authentication
- ✅ **Modern Stack** - Spring Boot backend + React TypeScript frontend

## Project Structure

```
Reposcribe/
├── backend/          # Spring Boot application
│   └── src/
│       └── main/
│           ├── java/com/reposcribe/
│           │   ├── controller/    # REST controllers
│           │   ├── service/       # Business logic
│           │   ├── model/         # Data models
│           │   ├── security/     # Security configuration
│           │   └── dto/          # Data transfer objects
│           └── resources/
│               └── application.properties
└── frontend/         # React TypeScript application
    └── src/
        ├── components/    # React components
        ├── pages/         # Page components
        ├── services/      # API services
        └── store/         # State management (Zustand)
```

## Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- Maven 3.6+
- npm or yarn

## Backend Setup

1. Navigate to the backend directory:
```bash
cd backend
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`

## Frontend Setup

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

The frontend will start on `http://localhost:5173`

## Usage

1. **Register/Login**: Create an account or login with existing credentials
2. **Upload Code**: 
   - Upload a ZIP file containing your code repository
   - Or provide a Git repository URL to clone
3. **Generate Documentation**: The system will analyze your code and generate documentation
4. **Download**: View and download the generated documentation

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get JWT token

### File Upload
- `POST /api/upload/file` - Upload ZIP file (requires authentication)
- `GET /api/upload/health` - Check upload service health

### Git Clone
- `POST /api/git/clone` - Clone a Git repository (requires authentication)
- `GET /api/git/health` - Check Git service health

### Health Check
- `GET /api/health` - Public health check endpoint
- `GET /api/health/protected` - Protected health check endpoint

## Configuration

### Backend Configuration (`application.properties`)

- `server.port=8080` - Backend server port
- `jwt.secret` - JWT secret key (change in production!)
- `jwt.expiration=86400000` - Token expiration (24 hours)
- `spring.servlet.multipart.max-file-size=500MB` - Max file upload size

### Frontend Configuration (`vite.config.ts`)

- `port: 5173` - Frontend development server port
- Proxy configured to forward `/api/*` requests to backend

## Security Notes

- Passwords are hashed using BCrypt
- JWT tokens are used for authentication
- CORS is configured to allow frontend-backend communication
- File uploads are validated and processed securely
- Temporary files are cleaned up after processing

## Development

### Backend
- Uses Spring Boot 3.2.0
- Java 17+
- Maven for dependency management

### Frontend
- React 18+ with TypeScript
- Vite for build tooling
- Material-UI for components
- Zustand for state management
- React Router for navigation

## License

This project is created for educational purposes.

## Next Steps

The tutorial covers additional modules for:
- Code parsing (Java, Python, JavaScript, etc.)
- AI integration with Ollama
- Documentation generation
- PDF export

Refer to the tutorial guide for complete implementation details.

