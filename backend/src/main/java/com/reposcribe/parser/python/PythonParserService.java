package com.reposcribe.parser.python;

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
public class PythonParserService implements Parser {

    private static final Pattern CLASS_PATTERN = Pattern.compile(
        "^\\s*(?:@\\w+\\s+)*class\\s+(\\w+)(?:\\(([^)]+)\\))?\\s*:"
    );
    private static final Pattern DEF_PATTERN = Pattern.compile(
        "^\\s*(?:@\\w+\\s+)*def\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*(?:->\\s*(\\w+))?\\s*:"
    );
    private static final Pattern DOCSTRING_PATTERN = Pattern.compile(
        "\"\"\"([^\"]*)\"\"\"|'''([^']*)'''"
    );

    @Override
    public String getLanguage() {
        return "python";
    }

    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList(".py");
    }

    @Override
    public boolean canParse(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return fileName.endsWith(".py");
    }

    @Override
    public ClassInfo parseFile(Path filePath) throws Exception {
        List<String> lines = Files.readAllLines(filePath);
        return parsePythonFile(lines, filePath);
    }

    @Override
    public List<Path> findFiles(Path directory) {
        return findPythonFiles(directory);
    }

    private ClassInfo parsePythonFile(List<String> lines, Path filePath) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setFilePath(filePath.toString());

        String fileName = filePath.getFileName().toString();
        String moduleName = fileName.replace(".py", "");
        classInfo.setName(moduleName);

        String currentClass = null;
        int indentLevel = 0;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            String trimmed = line.trim();

            if (trimmed.isEmpty()) continue;

            if (i < 3 && (trimmed.startsWith("\"\"\"") || trimmed.startsWith("'''"))) {
                String doc = extractDocstring(lines, i);
                if (doc != null && classInfo.getJavadoc() == null) {
                    classInfo.setJavadoc(doc);
                }
            }

            Matcher classMatcher = CLASS_PATTERN.matcher(line);
            if (classMatcher.find()) {
                String className = classMatcher.group(1);
                if (currentClass == null) {
                    classInfo.setName(className);
                    currentClass = className;
                    indentLevel = getIndentLevel(line);
                }
                continue;
            }

            Matcher defMatcher = DEF_PATTERN.matcher(line);
            if (defMatcher.find()) {
                MethodInfo methodInfo = parseMethod(defMatcher, lines, i);
                classInfo.getMethods().add(methodInfo);
            }
        }

        return classInfo;
    }

    private MethodInfo parseMethod(Matcher defMatcher, List<String> lines, int lineIndex) {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setName(defMatcher.group(1));
        methodInfo.setConstructor(methodInfo.getName().equals("__init__"));

        String paramsStr = defMatcher.group(2);
        if (paramsStr != null && !paramsStr.trim().isEmpty()) {
            String[] params = paramsStr.split(",");
            for (String param : params) {
                param = param.trim();
                if (!param.isEmpty() && !param.equals("self") && !param.equals("cls")) {
                    ParameterInfo paramInfo = new ParameterInfo();
                    if (param.contains(":")) {
                        String[] parts = param.split(":");
                        paramInfo.setName(parts[0].trim());
                        paramInfo.setType(parts[1].trim());
                    } else {
                        paramInfo.setName(param);
                        paramInfo.setType("Any");
                    }
                    methodInfo.getParameters().add(paramInfo);
                }
            }
        }

        String returnType = defMatcher.group(3);
        if (returnType != null) {
            methodInfo.setReturnType(returnType);
        }

        if (lineIndex + 1 < lines.size()) {
            String nextLine = lines.get(lineIndex + 1).trim();
            if (nextLine.startsWith("\"\"\"") || nextLine.startsWith("'''")) {
                String doc = extractDocstring(lines, lineIndex + 1);
                if (doc != null) {
                    methodInfo.setJavadoc(doc);
                }
            }
        }

        return methodInfo;
    }

    private String extractDocstring(List<String> lines, int startIndex) {
        if (startIndex >= lines.size()) return null;
        String firstLine = lines.get(startIndex).trim();
        String quoteType = firstLine.startsWith("\"\"\"") ? "\"\"\"" : "'''";
        if (firstLine.endsWith(quoteType) && firstLine.length() > 6) {
            return firstLine.substring(3, firstLine.length() - 3).trim();
        }
        StringBuilder docstring = new StringBuilder();
        docstring.append(firstLine.substring(3).trim());
        for (int i = startIndex + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains(quoteType)) {
                int endIndex = line.indexOf(quoteType);
                docstring.append(" ").append(line.substring(0, endIndex).trim());
                break;
            } else {
                docstring.append(" ").append(line.trim());
            }
        }
        return docstring.toString().trim();
    }

    private int getIndentLevel(String line) {
        int level = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') level++;
            else if (c == '\t') level += 4;
            else break;
        }
        return level;
    }

    private List<Path> findPythonFiles(Path directory) {
        List<Path> pythonFiles = new ArrayList<>();
        findPythonFilesRecursive(directory.toFile(), pythonFiles);
        return pythonFiles;
    }

    private void findPythonFilesRecursive(java.io.File directory, List<Path> pythonFiles) {
        java.io.File[] files = directory.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isDirectory()) {
                    String dirName = file.getName();
                    if (!dirName.equals("__pycache__") && !dirName.equals(".git") && !dirName.equals("venv")) {
                        findPythonFilesRecursive(file, pythonFiles);
                    }
                } else if (file.getName().endsWith(".py")) {
                    pythonFiles.add(file.toPath());
                }
            }
        }
    }
}

