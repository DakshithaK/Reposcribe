package com.reposcribe.parser.java;

import com.reposcribe.parser.Parser;
import com.reposcribe.parser.model.*;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class JavaParserService implements Parser {

    private final JavaParser javaParser;

    public JavaParserService() {
        ParserConfiguration config = new ParserConfiguration();
        this.javaParser = new JavaParser(config);
    }

    /**
     * Parse a single Java file
     */
    public ClassInfo parseJavaFile(Path filePath) throws FileNotFoundException {
        File file = filePath.toFile();
        CompilationUnit cu = javaParser.parse(file).getResult().orElseThrow();

        ClassInfo classInfo = new ClassInfo();
        classInfo.setFilePath(filePath.toString());

        // Extract package
        cu.getPackageDeclaration().ifPresent(pkg -> 
            classInfo.setPackageName(pkg.getNameAsString())
        );

        // Find class/interface declarations
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(classDecl -> {
            if (classInfo.getName() == null) { // Use first class found
                classInfo.setName(classDecl.getNameAsString());
                classInfo.setInterface(classDecl.isInterface());
                classInfo.setAbstract(classDecl.isAbstract());

                // Extract modifiers
                classDecl.getModifiers().forEach(mod -> 
                    classInfo.getModifiers().add(mod.getKeyword().asString())
                );

                // Extract annotations
                classDecl.getAnnotations().forEach(ann -> 
                    classInfo.getAnnotations().add(ann.getNameAsString())
                );

                // Extract Javadoc
                classDecl.getJavadocComment().ifPresent(javadoc -> 
                    classInfo.setJavadoc(javadoc.parse().getDescription().toText())
                );

                // Extract superclass
                classDecl.getExtendedTypes().forEach(ext -> 
                    classInfo.getSuperClasses().add(ext.getNameAsString())
                );

                // Extract interfaces
                classDecl.getImplementedTypes().forEach(impl -> 
                    classInfo.getInterfaces().add(impl.getNameAsString())
                );

                // Extract methods
                classDecl.getMethods().forEach(method -> 
                    classInfo.getMethods().add(parseMethod(method, classInfo))
                );

                // Extract fields
                classDecl.getFields().forEach(field -> 
                    classInfo.getFields().addAll(parseField(field))
                );
            }
        });

        return classInfo;
    }

    /**
     * Parse a method declaration
     */
    private MethodInfo parseMethod(MethodDeclaration method, ClassInfo classInfo) {
        MethodInfo methodInfo = new MethodInfo();
        methodInfo.setName(method.getNameAsString());
        
        // Check if constructor (constructor has same name as class)
        boolean isConstructor = method.getNameAsString().equals(classInfo.getName());
        methodInfo.setConstructor(isConstructor);

        // Return type
        if (!isConstructor) {
            if (method.getType() != null) {
                methodInfo.setReturnType(method.getType().asString());
            } else {
                methodInfo.setReturnType("void");
            }
        }

        // Modifiers
        method.getModifiers().forEach(mod -> 
            methodInfo.getModifiers().add(mod.getKeyword().asString())
        );

        // Annotations
        method.getAnnotations().forEach(ann -> 
            methodInfo.getAnnotations().add(ann.getNameAsString())
        );

        // Parameters
        method.getParameters().forEach(param -> 
            methodInfo.getParameters().add(parseParameter(param))
        );

        // Javadoc
        method.getJavadocComment().ifPresent(javadoc -> 
            methodInfo.setJavadoc(javadoc.parse().getDescription().toText())
        );

        return methodInfo;
    }

    /**
     * Parse a parameter
     */
    private ParameterInfo parseParameter(Parameter parameter) {
        ParameterInfo paramInfo = new ParameterInfo();
        paramInfo.setName(parameter.getNameAsString());
        paramInfo.setType(parameter.getType().asString());

        // Annotations
        parameter.getAnnotations().forEach(ann -> 
            paramInfo.getAnnotations().add(ann.getNameAsString())
        );

        return paramInfo;
    }

    /**
     * Parse a field declaration (can have multiple variables)
     */
    private List<FieldInfo> parseField(FieldDeclaration field) {
        List<FieldInfo> fields = new ArrayList<>();

        field.getVariables().forEach(variable -> {
            FieldInfo fieldInfo = new FieldInfo();
            fieldInfo.setName(variable.getNameAsString());
            fieldInfo.setType(field.getElementType().asString());

            // Modifiers
            field.getModifiers().forEach(mod -> 
                fieldInfo.getModifiers().add(mod.getKeyword().asString())
            );

            // Annotations
            field.getAnnotations().forEach(ann -> 
                fieldInfo.getAnnotations().add(ann.getNameAsString())
            );

            // Javadoc
            field.getJavadocComment().ifPresent(javadoc -> 
                fieldInfo.setJavadoc(javadoc.parse().getDescription().toText())
            );

            fields.add(fieldInfo);
        });

        return fields;
    }

    /**
     * Find all Java files in a directory recursively
     */
    public List<Path> findJavaFiles(Path directory) {
        List<Path> javaFiles = new ArrayList<>();
        findJavaFilesRecursive(directory.toFile(), javaFiles);
        return javaFiles;
    }

    private void findJavaFilesRecursive(File directory, List<Path> javaFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findJavaFilesRecursive(file, javaFiles);
                } else if (file.getName().endsWith(".java")) {
                    javaFiles.add(file.toPath());
                }
            }
        }
    }

    @Override
    public String getLanguage() {
        return "java";
    }

    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList(".java");
    }

    @Override
    public boolean canParse(Path filePath) {
        String fileName = filePath.getFileName().toString().toLowerCase();
        return fileName.endsWith(".java");
    }

    @Override
    public ClassInfo parseFile(Path filePath) throws Exception {
        return parseJavaFile(filePath);
    }

    @Override
    public List<Path> findFiles(Path directory) {
        return findJavaFiles(directory);
    }
}

