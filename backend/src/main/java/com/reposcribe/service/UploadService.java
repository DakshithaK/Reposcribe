package com.reposcribe.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class UploadService {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final long MAX_FILE_SIZE = 500 * 1024 * 1024; // 500MB

    /**
     * Process uploaded ZIP file
     * @param file MultipartFile containing ZIP
     * @return Path to extracted files (temporary)
     */
    public Path processZipUpload(MultipartFile file) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 500MB");
        }

        if (!file.getOriginalFilename().endsWith(".zip")) {
            throw new IllegalArgumentException("File must be a ZIP archive");
        }

        // Create unique temporary directory
        String uniqueId = UUID.randomUUID().toString();
        Path extractPath = Paths.get(TEMP_DIR, "reposcribe-upload-" + uniqueId);
        Files.createDirectories(extractPath);

        // Extract ZIP file
        try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path filePath = extractPath.resolve(entry.getName());
                
                // Security: Prevent zip slip vulnerability
                if (!filePath.normalize().startsWith(extractPath.normalize())) {
                    throw new IOException("Invalid entry in ZIP file");
                }

                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.createDirectories(filePath.getParent());
                    Files.copy(zipInputStream, filePath);
                }
                zipInputStream.closeEntry();
            }
        }

        return extractPath;
    }

    /**
     * Clean up temporary files
     */
    public void cleanup(Path path) {
        try {
            if (path != null && Files.exists(path)) {
                Files.walk(path)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
            }
        } catch (IOException e) {
            // Ignore cleanup errors
        }
    }
}

