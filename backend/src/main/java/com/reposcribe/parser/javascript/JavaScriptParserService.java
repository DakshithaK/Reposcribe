package com.reposcribe.parser.javascript;

import com.reposcribe.parser.Parser;
import com.reposcribe.parser.model.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class JavaScriptParserService implements Parser {

    private static final Pattern CLASS_PATTERN = Pattern.compile(
        "\\bclass\\s+(\\w+)(?:\\s+extends\\s+(\\w+))?\\s*\\{"
    );
    private static final Pattern FUNCTION_PATTERN = Pattern.compile(
        "(?:export\\s+)?(?:async\\s+)?(?:function\\s+(\\w+)|const\\s+(\\w+)\\s*=\\s*(?:async\\s+)?(?:\\([^)]*\\)\\s*=>|function))"
    );
    private static final Pattern REACT_COMPONENT_PATTERN = Pattern.compile(
        "(?:export\\s+)?(?:default\\s+)?(?:function\\s+|const\\s+)(\\w+)\\s*[:=]\\s*(?:React\\.)?(?:FC|FunctionComponent)"
    );
    private static final Pattern JSDOC_PATTERN = Pattern.compile(
        "/\\*\\*([^*]|\\*(?!/))*\\*/"
    );
    private static final Pattern IMPORT_PATTERN = Pattern.compile(
        "import\\s+(?:\\{[^}]+\\}|\\*\\s+as\\s+\\w+|\\w+)\\s+from\\s+['\"]([^'\"]+)['\"]"
    );

    @Override
    public String getLanguage() {
        return "javascript";
    }

    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList(".js", ".jsx", ".ts", ".tsx");
    }

    @Override
    public boolean canParse(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return fileName.endsWith(".js") || fileName.endsWith(".jsx") || 
               fileName.endsWith(".ts") || fileName.endsWith(".tsx");
    }

    @Override
    public ClassInfo parseFile(Path filePath) throws Exception {
        String content = Files.readString(filePath);
        return parseJavaScriptFile(content, filePath);
    }

    @Override
    public List<Path> findFiles(Path directory) {
        return findJavaScriptFiles(directory);
    }

    private ClassInfo parseJavaScriptFile(String content, Path filePath) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setFilePath(filePath.toString());

        String fileName = filePath.getFileName().toString();
        String moduleName = fileName.replaceAll("\\.(js|jsx|ts|tsx)$", "");
        classInfo.setName(moduleName);

        Matcher jsdocMatcher = JSDOC_PATTERN.matcher(content);
        if (jsdocMatcher.find()) {
            String jsdoc = jsdocMatcher.group(0)
                .replace("/**", "")
                .replace("*/", "")
                .trim();
            classInfo.setJavadoc(jsdoc);
        }

        Matcher importMatcher = IMPORT_PATTERN.matcher(content);
        while (importMatcher.find()) {
            classInfo.getAnnotations().add("import: " + importMatcher.group(1));
        }

        Matcher reactMatcher = REACT_COMPONENT_PATTERN.matcher(content);
        if (reactMatcher.find()) {
            classInfo.setName(reactMatcher.group(1));
            classInfo.getAnnotations().add("ReactComponent");
        }

        Matcher classMatcher = CLASS_PATTERN.matcher(content);
        if (classMatcher.find()) {
            String className = classMatcher.group(1);
            if (classInfo.getName().equals(moduleName)) {
                classInfo.setName(className);
            }
            String extendsClass = classMatcher.group(2);
            if (extendsClass != null) {
                classInfo.getSuperClasses().add(extendsClass);
            }
        }

        Matcher funcMatcher = FUNCTION_PATTERN.matcher(content);
        while (funcMatcher.find()) {
            String funcName = funcMatcher.group(1);
            if (funcName == null) funcName = funcMatcher.group(2);
            if (funcName != null) {
                MethodInfo methodInfo = new MethodInfo();
                methodInfo.setName(funcName);
                methodInfo.setReturnType("any");
                classInfo.getMethods().add(methodInfo);
            }
        }

        return classInfo;
    }

    private List<Path> findJavaScriptFiles(Path directory) {
        List<Path> jsFiles = new ArrayList<>();
        findJavaScriptFilesRecursive(directory.toFile(), jsFiles);
        return jsFiles;
    }

    private void findJavaScriptFilesRecursive(java.io.File directory, List<Path> jsFiles) {
        java.io.File[] files = directory.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isDirectory()) {
                    String dirName = file.getName();
                    if (!dirName.equals("node_modules") && !dirName.equals(".git")) {
                        findJavaScriptFilesRecursive(file, jsFiles);
                    }
                } else {
                    String fileName = file.getName().toLowerCase();
                    if (fileName.endsWith(".js") || fileName.endsWith(".jsx") || 
                        fileName.endsWith(".ts") || fileName.endsWith(".tsx")) {
                        jsFiles.add(file.toPath());
                    }
                }
            }
        }
    }
}

