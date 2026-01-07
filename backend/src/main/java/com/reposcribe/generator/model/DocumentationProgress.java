package com.reposcribe.generator.model;

public class DocumentationProgress {
    private String status;
    private int progress; // 0-100
    private String documentation;
    private String error;

    public DocumentationProgress() {
        this.status = "Initializing";
        this.progress = 0;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

