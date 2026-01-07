package com.reposcribe.ai;

import com.reposcribe.parser.model.ClassInfo;
import com.reposcribe.parser.model.MethodInfo;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIService {

    private final OllamaClient ollamaClient;

    public AIService(OllamaClient ollamaClient) {
        this.ollamaClient = ollamaClient;
    }

    public String analyzeClass(ClassInfo classInfo) throws IOException {
        String prompt = buildClassAnalysisPrompt(classInfo);
        return ollamaClient.generate(prompt);
    }

    public String analyzeMethod(MethodInfo methodInfo, ClassInfo classInfo) throws IOException {
        String prompt = buildMethodAnalysisPrompt(methodInfo, classInfo);
        return ollamaClient.generate(prompt);
    }

    public String generateProjectOverview(List<ClassInfo> classes) throws IOException {
        String prompt = buildProjectOverviewPrompt(classes);
        return ollamaClient.generate(prompt);
    }

    private String buildClassAnalysisPrompt(ClassInfo classInfo) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this code class and provide a clear, concise description:\n\n");
        
        if (classInfo.getPackageName() != null) {
            prompt.append("Package: ").append(classInfo.getPackageName()).append("\n");
        }
        prompt.append("Class: ").append(classInfo.getName()).append("\n");
        
        if (classInfo.isInterface()) {
            prompt.append("Type: Interface\n");
        } else if (classInfo.isAbstract()) {
            prompt.append("Type: Abstract Class\n");
        }
        
        if (!classInfo.getSuperClasses().isEmpty()) {
            prompt.append("Extends: ").append(String.join(", ", classInfo.getSuperClasses())).append("\n");
        }
        if (!classInfo.getInterfaces().isEmpty()) {
            prompt.append("Implements: ").append(String.join(", ", classInfo.getInterfaces())).append("\n");
        }
        if (!classInfo.getAnnotations().isEmpty()) {
            prompt.append("Annotations: ").append(String.join(", ", classInfo.getAnnotations())).append("\n");
        }
        
        if (!classInfo.getMethods().isEmpty()) {
            prompt.append("\nMethods:\n");
            classInfo.getMethods().forEach(method -> {
                prompt.append("  - ").append(method.getReturnType() != null ? method.getReturnType() : "void")
                      .append(" ").append(method.getName()).append("(");
                if (!method.getParameters().isEmpty()) {
                    String params = method.getParameters().stream()
                        .map(p -> p.getType() + " " + p.getName())
                        .collect(Collectors.joining(", "));
                    prompt.append(params);
                }
                prompt.append(")\n");
            });
        }
        
        if (classInfo.getJavadoc() != null) {
            prompt.append("\nExisting Documentation: ").append(classInfo.getJavadoc()).append("\n");
        }
        
        prompt.append("\nProvide a clear description of what this class does (2-3 sentences).");
        return prompt.toString();
    }

    private String buildMethodAnalysisPrompt(MethodInfo methodInfo, ClassInfo classInfo) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this method and provide a clear description:\n\n");
        prompt.append("Class: ").append(classInfo.getName()).append("\n");
        prompt.append("Method: ").append(methodInfo.getName()).append("\n");
        
        if (methodInfo.getReturnType() != null) {
            prompt.append("Returns: ").append(methodInfo.getReturnType()).append("\n");
        }
        if (!methodInfo.getParameters().isEmpty()) {
            prompt.append("Parameters:\n");
            methodInfo.getParameters().forEach(param -> {
                prompt.append("  - ").append(param.getType()).append(" ").append(param.getName()).append("\n");
            });
        }
        if (methodInfo.getJavadoc() != null) {
            prompt.append("Existing Documentation: ").append(methodInfo.getJavadoc()).append("\n");
        }
        prompt.append("\nProvide a clear description of what this method does (1-2 sentences).");
        return prompt.toString();
    }

    private String buildProjectOverviewPrompt(List<ClassInfo> classes) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze this codebase structure and provide a project overview:\n\n");
        prompt.append("Total Classes: ").append(classes.size()).append("\n\n");
        prompt.append("Classes:\n");
        classes.forEach(classInfo -> {
            prompt.append("- ").append(classInfo.getName());
            if (classInfo.getPackageName() != null) {
                prompt.append(" (").append(classInfo.getPackageName()).append(")");
            }
            prompt.append("\n  Methods: ").append(classInfo.getMethods().size());
            prompt.append(", Fields: ").append(classInfo.getFields().size()).append("\n");
        });
        prompt.append("\nProvide a comprehensive overview including: what the project does, main components, architecture patterns, and key technologies (4-5 sentences).");
        return prompt.toString();
    }

    public boolean isAvailable() {
        return ollamaClient.isAvailable();
    }

    /**
     * Analyze architecture from a custom prompt
     */
    public String analyzeArchitecture(String prompt) throws IOException {
        return ollamaClient.generate(prompt);
    }
}

