package com.roanyosky.image_processing_service.controllers;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;
import com.roanyosky.image_processing_service.dtos.*;
import com.roanyosky.image_processing_service.entities.User;
import com.roanyosky.image_processing_service.filters.SepiaFilter;
import com.roanyosky.image_processing_service.repositories.ImageRepository;
import com.roanyosky.image_processing_service.services.ImageService;
import com.roanyosky.image_processing_service.services.R2Service;
import com.roanyosky.image_processing_service.services.RateLimiteService;
import io.github.bucket4j.Bucket;
import lombok.AllArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.List;

@RestController
@AllArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/v1/images")
public class ImageController {
    private final ImageService imageService;
    private final R2Service r2Service;
    private final RateLimiteService rateLimiteService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal User currentUser) {
        //1. Resolves the bucket for this speficic user
        Bucket bucket = rateLimiteService.resolveBucket((long)currentUser.getId());

        if (bucket.tryConsume(1))
        {
            try {
                //1. Finding measurements
                HashMap<String, Integer> measures = new HashMap<>();
                measures = imageService.findingMeasurement(file);

                // 2. Upload the raw file to Cloudflare R2 / S3 storage
                String fileKey = r2Service.uploadFile(file);

                // 3. Map file data and metadata to the Data Transfer Object (DTO)
                ImageCreateDto imageCreateDto = new ImageCreateDto();
                imageCreateDto.setOwner_id(currentUser.getId());
                imageCreateDto.setOriginalName(file.getOriginalFilename());
                imageCreateDto.setR2Key(fileKey);
                imageCreateDto.setContentType(file.getContentType());
                imageCreateDto.setFileSize(file.getSize());
                imageCreateDto.setWidth(measures.get("width"));
                imageCreateDto.setHeight(measures.get("height"));
                // 4. Save metadata to the database and return the result
                return ResponseEntity.ok(imageService.createImage(imageCreateDto));
            } catch (IOException e) {
                return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
            }
        }else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("You have reacheed your free upload limit (2 photos per day)");
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

    @PostMapping("/{key}/transform")
    public ResponseEntity<?> transformImage(@PathVariable String key, @RequestBody TransformRequest transformRequest){
        //Extract the Transform Object Dto
        TransformDto transformDto = transformRequest.getTransformations();

        //1. Gets the original image from R2
        byte[] data = r2Service.getFile(key);

        ImageProcessingResult processedImage = imageService.imageTransfomation(transformDto, data);

        //3. Returns the processed image
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/" + processedImage.getFormat()))
                .body(processedImage.getImageData());
    }

    @GetMapping
    public ResponseEntity<Page<ImageDto>> getImages(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int limit
    ){
        Page<ImageDto> images = imageService.getAllImagesPaginated(page, limit);
        return ResponseEntity.ok(images);
    }

}
