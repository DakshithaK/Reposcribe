package com.reposcribe.parser;

import com.reposcribe.parser.model.ClassInfo;

import java.nio.file.Path;
import java.util.List;

/**
 * Interface for all language parsers
 */
public interface Parser {
    
    /**
     * Get the language this parser supports
     */
    String getLanguage();
    
    /**
     * Get file extensions this parser can handle
     */
    List<String> getSupportedExtensions();
    
    /**
     * Check if this parser can handle the given file
     */
    boolean canParse(Path filePath);
    
    /**
     * Parse a single file
     */
    ClassInfo parseFile(Path filePath) throws Exception;
    
    /**
     * Find all files of this language in a directory
     */
    List<Path> findFiles(Path directory);
}

