package com.visualpathit.account.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller to serve uploaded static files (profile images, etc.)
 */
@Controller
public class StaticResourceController {

    private static final Logger logger = LoggerFactory.getLogger(StaticResourceController.class);
    private static final String UPLOAD_BASE_DIR = "/var/lib/facelink/uploads";

    /**
     * Serve profile images from /uploads/profiles/{filename}
     */
    @GetMapping("/uploads/profiles/{filename:.+}")
    public ResponseEntity<Resource> serveProfileImage(@PathVariable String filename) {
        logger.debug("Request to serve profile image: {}", filename);

        try {
            // Construct the file path
            Path filePath = Paths.get(UPLOAD_BASE_DIR, "profiles", filename).normalize();
            File file = filePath.toFile();

            logger.debug("Looking for file at: {}", filePath.toString());

            // Security check: ensure the resolved path is still under the upload directory
            if (!filePath.startsWith(Paths.get(UPLOAD_BASE_DIR))) {
                logger.warn("Security violation: attempt to access file outside upload directory: {}", filename);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Check if file exists and is readable
            if (!file.exists()) {
                logger.warn("File not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            if (!file.canRead()) {
                logger.warn("File not readable: {}", filePath);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            logger.info("Serving file: {} (size: {} bytes, type: {})",
                       filePath, file.length(), contentType);

            // Create resource
            Resource resource = new FileSystemResource(file);

            // Return the file with appropriate headers
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .body(resource);

        } catch (IOException e) {
            logger.error("Error serving profile image: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
