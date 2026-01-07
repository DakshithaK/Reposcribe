package com.reposcribe.service;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    // Store active sessions: sessionId -> extracted path
    private final Map<String, Path> activeSessions = new ConcurrentHashMap<>();
    private final UploadService uploadService;

    public SessionService(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    /**
     * Register a new session with extracted files
     */
    public String registerSession(Path extractedPath) {
        String sessionId = extractedPath.getFileName().toString();
        activeSessions.put(sessionId, extractedPath);
        return sessionId;
    }

    /**
     * Get path for a session
     */
    public Path getSessionPath(String sessionId) {
        return activeSessions.get(sessionId);
    }

    /**
     * Check if session exists
     */
    public boolean sessionExists(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    /**
     * Clean up session and delete files
     */
    public void cleanupSession(String sessionId) {
        Path path = activeSessions.remove(sessionId);
        if (path != null) {
            uploadService.cleanup(path);
        }
    }

    /**
     * Clean up all sessions (call on shutdown)
     */
    public void cleanupAll() {
        activeSessions.values().forEach(uploadService::cleanup);
        activeSessions.clear();
    }
}

