package com.reposcribe.generator;

import com.reposcribe.parser.model.ClassInfo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TemplateService {

    public String generateReadme(
            String projectName,
            String projectOverview,
            Map<String, List<ClassInfo>> parsedClasses,
            Map<String, Object> statistics,
            String architectureAnalysis) {
        
        StringBuilder readme = new StringBuilder();
        readme.append("# ").append(projectName != null ? projectName : "Project Documentation").append("\n\n");
        readme.append("## Overview\n\n");
        if (projectOverview != null && !projectOverview.isEmpty()) {
            readme.append(projectOverview).append("\n\n");
        } else {
            readme.append("This project has been automatically analyzed and documented.\n\n");
        }
        
        readme.append("## Table of Contents\n\n");
        readme.append("- [Overview](#overview)\n");
        readme.append("- [Project Statistics](#project-statistics)\n");
        readme.append("- [Architecture](#architecture)\n");
        readme.append("- [Components](#components)\n");
        readme.append("\n");
        
        readme.append("## Project Statistics\n\n");
        if (statistics != null) {
            Map<String, Integer> fileCounts = (Map<String, Integer>) statistics.get("fileCounts");
            Map<String, Integer> classCounts = (Map<String, Integer>) statistics.get("classCounts");
            
            if (fileCounts != null) {
                readme.append("### Files by Language\n\n");
                fileCounts.forEach((lang, count) -> {
                    readme.append("- **").append(lang).append("**: ").append(count).append(" files\n");
                });
                readme.append("\n");
            }
            
            if (classCounts != null) {
                readme.append("### Classes by Language\n\n");
                classCounts.forEach((lang, count) -> {
                    readme.append("- **").append(lang).append("**: ").append(count).append(" classes\n");
                });
                readme.append("\n");
            }
        }
        
        if (architectureAnalysis != null && !architectureAnalysis.isEmpty()) {
            readme.append("## Architecture\n\n");
            readme.append(architectureAnalysis).append("\n\n");
        }
        
        readme.append("## Components\n\n");
        parsedClasses.forEach((language, classes) -> {
            readme.append("### ").append(language.toUpperCase()).append(" Components\n\n");
            classes.forEach(classInfo -> {
                readme.append("#### ").append(classInfo.getName()).append("\n\n");
                if (classInfo.getPackageName() != null) {
                    readme.append("**Package**: `").append(classInfo.getPackageName()).append("`\n\n");
                }
                if (classInfo.getJavadoc() != null && !classInfo.getJavadoc().isEmpty()) {
                    readme.append(classInfo.getJavadoc()).append("\n\n");
                }
                if (!classInfo.getMethods().isEmpty()) {
                    readme.append("**Methods**:\n\n");
                    classInfo.getMethods().forEach(method -> {
                        readme.append("- `");
                        if (method.getReturnType() != null) {
                            readme.append(method.getReturnType()).append(" ");
                        }
                        readme.append(method.getName()).append("(");
                        if (!method.getParameters().isEmpty()) {
                            String params = method.getParameters().stream()
                                .map(p -> p.getType() + " " + p.getName())
                                .collect(Collectors.joining(", "));
                            readme.append(params);
                        }
                        readme.append(")`\n");
                    });
                    readme.append("\n");
                }
                readme.append("---\n\n");
            });
        });
        
        readme.append("## Getting Started\n\n");
        readme.append("### Prerequisites\n\n");
        readme.append("- Java 17+\n");
        readme.append("- Maven 3.6+\n\n");
        readme.append("### Installation\n\n");
        readme.append("```bash\n");
        readme.append("git clone <repository-url>\n");
        readme.append("cd ").append(projectName != null ? projectName : "project").append("\n");
        readme.append("mvn install\n");
        readme.append("```\n\n");
        readme.append("*This documentation was automatically generated.*\n");
        
        return readme.toString();
    }
}

