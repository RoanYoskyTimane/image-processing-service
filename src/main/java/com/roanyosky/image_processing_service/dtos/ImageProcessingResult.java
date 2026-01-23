package com.roanyosky.image_processing_service.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageProcessingResult {
    private byte[] imageData;
    private String format;
}
