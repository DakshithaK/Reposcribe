package com.reposcribe.controller;

import com.reposcribe.generator.DocumentationGeneratorService;
import com.reposcribe.generator.model.DocumentationProgress;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/documentation")
public class DocumentationController {

    private final DocumentationGeneratorService docGeneratorService;

    public DocumentationController(DocumentationGeneratorService docGeneratorService) {
        this.docGeneratorService = docGeneratorService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateDocumentation(
            @RequestParam String sessionId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String documentation = docGeneratorService.generateDocumentation(sessionId);
            
            response.put("success", true);
            response.put("documentation", documentation);
            response.put("format", "markdown");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @PostMapping("/generate-with-progress")
    public ResponseEntity<DocumentationProgress> generateWithProgress(
            @RequestParam String sessionId) {
        
        DocumentationProgress progress = docGeneratorService.generateDocumentationWithProgress(sessionId);
        return ResponseEntity.ok(progress);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadDocumentation(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "markdown") String format) {
        
        try {
            String documentation = docGeneratorService.generateDocumentation(sessionId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", "README.md");
            return new ResponseEntity<>(
                documentation.getBytes(),
                headers,
                HttpStatus.OK
            );
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "ready");
        return ResponseEntity.ok(response);
    }
}

