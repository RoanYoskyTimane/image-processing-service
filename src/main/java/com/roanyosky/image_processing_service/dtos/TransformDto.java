package com.roanyosky.image_processing_service.dtos;

import lombok.Builder;

@Builder
public class TransformDto {
    ResizeDto resizeDto;
    CropDto cropDto;
    Integer rotate;
    String format;
    FiltersDto filtersDto;
}
