package com.reposcribe.controller;

import com.reposcribe.service.SessionService;
import com.reposcribe.service.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private final UploadService uploadService;
    private final SessionService sessionService;

    public UploadController(UploadService uploadService, SessionService sessionService) {
        this.uploadService = uploadService;
        this.sessionService = sessionService;
    }

    @PostMapping("/file")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Process the uploaded file
            Path extractedPath = uploadService.processZipUpload(file);
            String sessionId = sessionService.registerSession(extractedPath);
            
            response.put("success", true);
            response.put("message", "File uploaded and extracted successfully");
            response.put("sessionId", sessionId);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to process file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Upload service is ready");
        return ResponseEntity.ok(response);
    }
}

