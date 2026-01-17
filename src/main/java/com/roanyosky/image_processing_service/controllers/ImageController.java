package com.roanyosky.image_processing_service.controllers;

import com.roanyosky.image_processing_service.services.R2Service;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/images")
public class ImageController {
    private final R2Service r2Service;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            String fileKey = r2Service.uploadFile(file);
            return ResponseEntity.ok(fileKey);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/{key}")
    public ResponseEntity<byte[]> download(@PathVariable String key) {
        byte[] data = r2Service.getFile(key);

        // Determine the media type (e.g., image/jpeg, image/png)
        MediaType mediaType = MediaType.IMAGE_JPEG; // Default
        if (key.toLowerCase().endsWith(".png")) mediaType = MediaType.IMAGE_PNG;
        if (key.toLowerCase().endsWith(".gif")) mediaType = MediaType.IMAGE_GIF;

        return ResponseEntity.ok()
                .contentType(mediaType) // CRITICAL: Tells the OS this is an image
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + key + "\"")
                .body(data);
    }
}
