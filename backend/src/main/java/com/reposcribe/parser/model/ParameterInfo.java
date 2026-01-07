package com.reposcribe.parser.model;

import java.util.ArrayList;
import java.util.List;

public class ParameterInfo {
    private String name;
    private String type;
    private List<String> annotations;

    public ParameterInfo() {
        this.annotations = new ArrayList<>();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<String> annotations) {
        this.annotations = annotations;
    }
}

