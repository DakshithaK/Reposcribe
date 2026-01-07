package com.reposcribe.controller;

import com.reposcribe.ai.AIService;
import com.reposcribe.parser.UnifiedParserService;
import com.reposcribe.parser.model.ClassInfo;
import com.reposcribe.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final UnifiedParserService parserService;
    private final AIService aiService;
    private final SessionService sessionService;

    public AnalysisController(
            UnifiedParserService parserService,
            AIService aiService,
            SessionService sessionService) {
        this.parserService = parserService;
        this.aiService = aiService;
        this.sessionService = sessionService;
    }

    @PostMapping("/analyze-project")
    public ResponseEntity<Map<String, Object>> analyzeProject(
            @RequestParam String sessionId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (!sessionService.sessionExists(sessionId)) {
                response.put("success", false);
                response.put("message", "Invalid session ID");
                return ResponseEntity.badRequest().body(response);
            }

            if (!aiService.isAvailable()) {
                response.put("success", false);
                response.put("message", "AI service is not available");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
            }

            Path sessionPath = sessionService.getSessionPath(sessionId);
            if (sessionPath == null) {
                response.put("success", false);
                response.put("message", "Session path not found");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, List<ClassInfo>> parsedResults = parserService.parseDirectory(sessionPath);
            List<ClassInfo> allClasses = parsedResults.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

            String overview = aiService.generateProjectOverview(allClasses);

            response.put("success", true);
            response.put("statistics", parserService.getDirectoryStatistics(sessionPath));
            response.put("parsedClasses", parsedResults);
            response.put("overview", overview);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("aiAvailable", aiService.isAvailable());
        return ResponseEntity.ok(response);
    }
}

