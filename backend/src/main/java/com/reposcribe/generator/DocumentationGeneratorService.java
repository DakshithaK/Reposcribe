package com.reposcribe.generator;

import com.reposcribe.ai.AIService;
import com.reposcribe.ai.SemanticAnalysisService;
import com.reposcribe.generator.model.DocumentationProgress;
import com.reposcribe.parser.UnifiedParserService;
import com.reposcribe.parser.model.ClassInfo;
import com.reposcribe.service.SessionService;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocumentationGeneratorService {

    private final UnifiedParserService parserService;
    private final AIService aiService;
    private final SemanticAnalysisService semanticService;
    private final TemplateService templateService;
    private final SessionService sessionService;

    public DocumentationGeneratorService(
            UnifiedParserService parserService,
            AIService aiService,
            SemanticAnalysisService semanticService,
            TemplateService templateService,
            SessionService sessionService) {
        this.parserService = parserService;
        this.aiService = aiService;
        this.semanticService = semanticService;
        this.templateService = templateService;
        this.sessionService = sessionService;
    }

    public String generateDocumentation(String sessionId) throws Exception {
        if (!sessionService.sessionExists(sessionId)) {
            throw new IllegalArgumentException("Invalid session ID");
        }

        Path sessionPath = sessionService.getSessionPath(sessionId);
        if (sessionPath == null) {
            throw new IllegalArgumentException("Session path not found");
        }

        Map<String, List<ClassInfo>> parsedClasses = parserService.parseDirectory(sessionPath);
        Map<String, Object> statistics = parserService.getDirectoryStatistics(sessionPath);
        
        List<ClassInfo> allClasses = parsedClasses.values().stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
        
        String projectOverview = aiService.generateProjectOverview(allClasses);
        String architectureAnalysis = semanticService.identifyArchitecture(allClasses);
        String projectName = extractProjectName(sessionPath);
        
        return templateService.generateReadme(
            projectName,
            projectOverview,
            parsedClasses,
            statistics,
            architectureAnalysis
        );
    }

    public DocumentationProgress generateDocumentationWithProgress(String sessionId) {
        DocumentationProgress progress = new DocumentationProgress();
        
        try {
            progress.setStatus("Parsing files...");
            progress.setProgress(10);
            
            Path sessionPath = sessionService.getSessionPath(sessionId);
            Map<String, List<ClassInfo>> parsedClasses = parserService.parseDirectory(sessionPath);
            Map<String, Object> statistics = parserService.getDirectoryStatistics(sessionPath);
            
            progress.setStatus("Analyzing project...");
            progress.setProgress(30);
            
            List<ClassInfo> allClasses = parsedClasses.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
            
            String projectOverview = aiService.generateProjectOverview(allClasses);
            
            progress.setStatus("Analyzing architecture...");
            progress.setProgress(50);
            
            String architectureAnalysis = semanticService.identifyArchitecture(allClasses);
            
            progress.setStatus("Generating documentation...");
            progress.setProgress(90);
            
            String projectName = extractProjectName(sessionPath);
            String readme = templateService.generateReadme(
                projectName,
                projectOverview,
                parsedClasses,
                statistics,
                architectureAnalysis
            );
            
            progress.setStatus("Complete");
            progress.setProgress(100);
            progress.setDocumentation(readme);
            
        } catch (Exception e) {
            progress.setStatus("Error: " + e.getMessage());
            progress.setProgress(0);
            progress.setError(e.getMessage());
        }
        
        return progress;
    }

    private String extractProjectName(Path path) {
        String pathStr = path.toString();
        if (pathStr.contains("reposcribe-")) {
            Path parent = path.getParent();
            if (parent != null) {
                return parent.getFileName().toString();
            }
        }
        Path fileName = path.getFileName();
        if (fileName != null) {
            return fileName.toString();
        }
        return "Project";
    }
}

