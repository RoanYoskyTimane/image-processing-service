package com.roanyosky.image_processing_service.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class ImageDto {
    private UUID id;
    private Integer owner_id;
    private String originalName;
    private String contentType;
    private Integer height;
    private Integer width;
}
