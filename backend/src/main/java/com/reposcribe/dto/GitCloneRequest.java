package com.reposcribe.dto;

public class GitCloneRequest {
    private String repositoryUrl;
    private String username;  // Optional, for private repos
    private String password;  // Optional, for private repos (token)

    public GitCloneRequest() {
    }

    public GitCloneRequest(String repositoryUrl, String username, String password) {
        this.repositoryUrl = repositoryUrl;
        this.username = username;
        this.password = password;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        this.repositoryUrl = repositoryUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

