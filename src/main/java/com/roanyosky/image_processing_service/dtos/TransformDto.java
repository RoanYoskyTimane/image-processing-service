package com.roanyosky.image_processing_service.dtos;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransformDto {
    ResizeDto resize;
    CropDto crop;
    Integer rotate;
    String format;
    FiltersDto filters;
}
