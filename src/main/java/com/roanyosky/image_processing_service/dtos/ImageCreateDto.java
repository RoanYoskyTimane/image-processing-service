package com.roanyosky.image_processing_service.dtos;

import lombok.Data;

@Data
public class ImageCreateDto {
    private Integer owner_id;
    private String r2Key;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private Integer width;
    private Integer height;
}
