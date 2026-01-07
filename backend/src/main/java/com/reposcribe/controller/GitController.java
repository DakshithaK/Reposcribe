package com.reposcribe.controller;

import com.reposcribe.dto.GitCloneRequest;
import com.reposcribe.service.GitService;
import com.reposcribe.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/git")
public class GitController {

    private final GitService gitService;
    private final SessionService sessionService;

    public GitController(GitService gitService, SessionService sessionService) {
        this.gitService = gitService;
        this.sessionService = sessionService;
    }

    @PostMapping("/clone")
    public ResponseEntity<Map<String, Object>> cloneRepository(
            @RequestBody GitCloneRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate request
            if (request.getRepositoryUrl() == null || request.getRepositoryUrl().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Repository URL is required");
                return ResponseEntity.badRequest().body(response);
            }

            if (!gitService.isValidGitUrl(request.getRepositoryUrl())) {
                response.put("success", false);
                response.put("message", "Invalid Git repository URL");
                return ResponseEntity.badRequest().body(response);
            }

            // Clone the repository
            Path clonedPath = gitService.cloneRepository(
                request.getRepositoryUrl(),
                request.getUsername(),
                request.getPassword()
            );

            // Register session
            String sessionId = sessionService.registerSession(clonedPath);

            response.put("success", true);
            response.put("message", "Repository cloned successfully");
            response.put("sessionId", sessionId);
            response.put("repositoryUrl", request.getRepositoryUrl());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to clone repository: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Git service is ready");
        return ResponseEntity.ok(response);
    }
}

