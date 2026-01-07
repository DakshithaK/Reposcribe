package com.reposcribe.parser;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ParserRegistry {

    private final Map<String, Parser> parsers = new HashMap<>();

    /**
     * Register a parser
     */
    public void registerParser(Parser parser) {
        parsers.put(parser.getLanguage(), parser);
    }

    /**
     * Get parser by language name
     */
    public Parser getParser(String language) {
        return parsers.get(language.toLowerCase());
    }

    /**
     * Get parser for a specific file
     */
    public Parser getParserForFile(Path filePath) {
        for (Parser parser : parsers.values()) {
            if (parser.canParse(filePath)) {
                return parser;
            }
        }
        return null;
    }

    /**
     * Get all registered languages
     */
    public List<String> getSupportedLanguages() {
        return parsers.values().stream()
            .map(Parser::getLanguage)
            .collect(Collectors.toList());
    }

    /**
     * Get all registered parsers
     */
    public Map<String, Parser> getAllParsers() {
        return new HashMap<>(parsers);
    }
}

