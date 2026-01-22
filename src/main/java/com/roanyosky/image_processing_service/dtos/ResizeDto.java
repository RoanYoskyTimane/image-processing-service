package com.roanyosky.image_processing_service.dtos;

import lombok.Builder;

@Builder
public class ResizeDto {
    Integer width;
    Integer height;
}