package com.roanyosky.image_processing_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageCreateDto {
    private Integer owner_id;
    private String r2Key;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private Integer width;
    private Integer height;
}
