package com.reposcribe.ai;

import com.reposcribe.parser.model.ClassInfo;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class SemanticAnalysisService {

    private final AIService aiService;

    public SemanticAnalysisService(AIService aiService) {
        this.aiService = aiService;
    }

    public String identifyArchitecture(List<ClassInfo> classes) throws IOException {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this codebase and identify architectural patterns:\n\n");
        prompt.append("Total Classes: ").append(classes.size()).append("\n\n");
        prompt.append("Classes:\n");
        
        classes.forEach(classInfo -> {
            prompt.append("- ").append(classInfo.getName());
            if (classInfo.getPackageName() != null) {
                prompt.append(" (").append(classInfo.getPackageName()).append(")");
            }
            if (!classInfo.getAnnotations().isEmpty()) {
                String annotations = classInfo.getAnnotations().stream()
                    .filter(a -> a.contains("Service") || a.contains("Controller") || a.contains("Component") || a.contains("Repository"))
                    .collect(java.util.stream.Collectors.joining(", "));
                if (!annotations.isEmpty()) {
                    prompt.append(" [").append(annotations).append("]");
                }
            }
            prompt.append("\n  Methods: ").append(classInfo.getMethods().size());
            prompt.append(", Fields: ").append(classInfo.getFields().size()).append("\n");
        });
        
        prompt.append("\nIdentify the architecture pattern (MVC, Layered, Microservices, Clean Architecture, etc.) ");
        prompt.append("and explain the structure and organization (3-4 sentences).");
        
        // Use AIService's ollamaClient through a helper method
        // Since we can't access ollamaClient directly, we'll create a custom prompt method
        return aiService.analyzeArchitecture(prompt.toString());
    }
}

