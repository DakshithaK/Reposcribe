package com.reposcribe.parser;

import com.reposcribe.parser.java.JavaParserService;
import com.reposcribe.parser.javascript.JavaScriptParserService;
import com.reposcribe.parser.python.PythonParserService;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Configuration
public class ParserConfig {

    private final ParserRegistry parserRegistry;
    private final List<Parser> parsers;

    public ParserConfig(
            ParserRegistry parserRegistry,
            JavaParserService javaParserService,
            PythonParserService pythonParserService,
            JavaScriptParserService javascriptParserService) {
        this.parserRegistry = parserRegistry;
        this.parsers = List.of(
            javaParserService,
            pythonParserService,
            javascriptParserService
        );
    }

    @PostConstruct
    public void registerParsers() {
        for (Parser parser : parsers) {
            parserRegistry.registerParser(parser);
        }
    }
}

