package com.reposcribe.service;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class GitService {

    /**
     * Clone a Git repository
     * @param repositoryUrl Git repository URL (HTTPS or SSH)
     * @param username Optional username for private repos
     * @param password Optional password/token for private repos
     * @return Path to cloned repository
     */
    public Path cloneRepository(String repositoryUrl, String username, String password) 
            throws GitAPIException, Exception {
        
        // Validate URL
        if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository URL is required");
        }

        // Validate URL format (basic check)
        if (!repositoryUrl.startsWith("http://") && 
            !repositoryUrl.startsWith("https://") && 
            !repositoryUrl.startsWith("git@")) {
            throw new IllegalArgumentException("Invalid repository URL format");
        }

        // Create unique temporary directory
        String uniqueId = UUID.randomUUID().toString();
        Path clonePath = Paths.get(System.getProperty("java.io.tmpdir"), "reposcribe-git-" + uniqueId);
        Files.createDirectories(clonePath);

        try {
            // Clone the repository
            Git git;
            
            if (username != null && password != null && !username.isEmpty() && !password.isEmpty()) {
                // Clone with credentials (for private repos)
                UsernamePasswordCredentialsProvider credentials = 
                    new UsernamePasswordCredentialsProvider(username, password);
                
                git = Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(clonePath.toFile())
                    .setCredentialsProvider(credentials)
                    .setTimeout(60)
                    .call();
            } else {
                // Clone without credentials (public repos)
                git = Git.cloneRepository()
                    .setURI(repositoryUrl)
                    .setDirectory(clonePath.toFile())
                    .setTimeout(60)
                    .call();
            }

            // Close the Git instance (doesn't delete the files)
            git.close();

            return clonePath;
            
        } catch (org.eclipse.jgit.api.errors.TransportException e) {
            cleanup(clonePath);
            if (e.getMessage().contains("authentication")) {
                throw new Exception("Authentication failed. Check your credentials.", e);
            }
            throw new Exception("Failed to connect to repository: " + e.getMessage(), e);
            
        } catch (org.eclipse.jgit.api.errors.JGitInternalException e) {
            cleanup(clonePath);
            throw new Exception("Git operation failed: " + e.getMessage(), e);
            
        } catch (GitAPIException e) {
            // Clean up on failure
            cleanup(clonePath);
            throw new Exception("Failed to clone repository: " + e.getMessage(), e);
        }
    }

    /**
     * Clean up cloned repository
     */
    public void cleanup(Path path) {
        try {
            if (path != null && Files.exists(path)) {
                deleteDirectory(path.toFile());
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    /**
     * Recursively delete directory
     */
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }

    /**
     * Validate Git URL format
     */
    public boolean isValidGitUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        
        return url.startsWith("http://") || 
               url.startsWith("https://") || 
               url.startsWith("git@") ||
               url.endsWith(".git");
    }
}

