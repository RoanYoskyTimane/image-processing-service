package com.roanyosky.image_processing_service.controllers;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.roanyosky.image_processing_service.dtos.ImageCreateDto;
import com.roanyosky.image_processing_service.dtos.ImageDto;
import com.roanyosky.image_processing_service.entities.User;
import com.roanyosky.image_processing_service.repositories.ImageRepository;
import com.roanyosky.image_processing_service.services.ImageService;
import com.roanyosky.image_processing_service.services.R2Service;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/images")
public class ImageController {
    private final ImageService imageService;
    private final R2Service r2Service;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal User currentUser) {
        try {
            // 1. Wrap the input stream in a BufferedInputStream for efficient reading
            InputStream is = new BufferedInputStream(file.getInputStream());

            // 2. Extract metadata without loading the full image into memory (Performance Efficient)
            Metadata metadata = ImageMetadataReader.readMetadata(is);

            int width = 0;
            int height = 0;

            // 3. Iterate through metadata directories to find dimensions
            // Note: Different formats (JPG, PNG, WebP) store dimensions in different directories.
            for (Directory directory : metadata.getDirectories()) {
                // Check for JPEG specific dimensions
                if (directory.containsTag(JpegDirectory.TAG_IMAGE_WIDTH)) {
                    width = directory.getInt(JpegDirectory.TAG_IMAGE_WIDTH);
                    height = directory.getInt(JpegDirectory.TAG_IMAGE_HEIGHT);
                    break;
                }
                // Check for PNG specific dimensions
                else if (directory.containsTag(PngDirectory.TAG_IMAGE_WIDTH)){
                    width = directory.getInt(PngDirectory.TAG_IMAGE_HEIGHT);
                    height = directory.getInt(PngDirectory.TAG_IMAGE_WIDTH);
                    break;
                }
                // Fallback for other formats (GIF, BMP, etc.) using generic tags if available
                else if (directory.getName().contains("Header") && directory.containsTag(1)) {
                    // Many directories use Tag 1 for Width and Tag 2 for Height
                    width = directory.getInt(1);
                    height = directory.getInt(2);
                }
            }

            // 4. Upload the raw file to Cloudflare R2 / S3 storage
            String fileKey = r2Service.uploadFile(file);

            // 5. Map file data and metadata to the Data Transfer Object (DTO)
            ImageCreateDto imageCreateDto = new ImageCreateDto();
            imageCreateDto.setOwner_id(currentUser.getId());
            imageCreateDto.setOriginalName(file.getOriginalFilename());
            imageCreateDto.setR2Key(fileKey);
            imageCreateDto.setContentType(file.getContentType());
            imageCreateDto.setFileSize(file.getSize());
            imageCreateDto.setWidth(width);
            imageCreateDto.setHeight(height);

            // 6. Save metadata to the database and return the result
            return ResponseEntity.ok(imageService.createImage(imageCreateDto));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        } catch (ImageProcessingException | MetadataException e) {
            throw new RuntimeException(e);
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
