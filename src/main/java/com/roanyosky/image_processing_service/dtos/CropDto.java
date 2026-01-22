package com.roanyosky.image_processing_service.dtos;

import lombok.Builder;

@Builder
public class CropDto {
    Integer width;
    Integer height;
    Integer x;
    Integer y;
}
