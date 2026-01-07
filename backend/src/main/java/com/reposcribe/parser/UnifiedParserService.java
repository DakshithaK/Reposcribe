package com.reposcribe.parser;

import com.reposcribe.parser.model.ClassInfo;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UnifiedParserService {

    private final ParserRegistry parserRegistry;

    public UnifiedParserService(ParserRegistry parserRegistry) {
        this.parserRegistry = parserRegistry;
    }

    /**
     * Parse a single file using the appropriate parser
     */
    public ClassInfo parseFile(Path filePath) throws Exception {
        Parser parser = parserRegistry.getParserForFile(filePath);
        
        if (parser == null) {
            throw new UnsupportedOperationException(
                "No parser available for file: " + filePath
            );
        }

        return parser.parseFile(filePath);
    }

    /**
     * Parse all files in a directory
     */
    public Map<String, List<ClassInfo>> parseDirectory(Path directory) throws Exception {
        Map<String, List<ClassInfo>> results = new HashMap<>();

        for (Parser parser : parserRegistry.getAllParsers().values()) {
            List<Path> files = parser.findFiles(directory);
            List<ClassInfo> classInfos = new ArrayList<>();

            for (Path file : files) {
                try {
                    ClassInfo classInfo = parser.parseFile(file);
                    classInfos.add(classInfo);
                } catch (Exception e) {
                    System.err.println("Failed to parse " + file + ": " + e.getMessage());
                }
            }

            if (!classInfos.isEmpty()) {
                results.put(parser.getLanguage(), classInfos);
            }
        }

        return results;
    }

    /**
     * Get statistics about files in a directory
     */
    public Map<String, Object> getDirectoryStatistics(Path directory) {
        Map<String, Object> stats = new HashMap<>();
        Map<String, Integer> fileCounts = new HashMap<>();
        Map<String, Integer> classCounts = new HashMap<>();

        for (Parser parser : parserRegistry.getAllParsers().values()) {
            List<Path> files = parser.findFiles(directory);
            fileCounts.put(parser.getLanguage(), files.size());

            int classCount = 0;
            for (Path file : files) {
                try {
                    ClassInfo classInfo = parser.parseFile(file);
                    if (classInfo.getName() != null) {
                        classCount++;
                    }
                } catch (Exception e) {
                    // Ignore parsing errors for statistics
                }
            }
            classCounts.put(parser.getLanguage(), classCount);
        }

        stats.put("fileCounts", fileCounts);
        stats.put("classCounts", classCounts);
        stats.put("supportedLanguages", parserRegistry.getSupportedLanguages());

        return stats;
    }

    /**
     * Check if a file can be parsed
     */
    public boolean canParse(Path filePath) {
        return parserRegistry.getParserForFile(filePath) != null;
    }

    /**
     * Get parser for a specific file
     */
    public String getParserLanguage(Path filePath) {
        Parser parser = parserRegistry.getParserForFile(filePath);
        return parser != null ? parser.getLanguage() : null;
    }
}

