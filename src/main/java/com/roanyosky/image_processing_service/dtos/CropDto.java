package com.roanyosky.image_processing_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CropDto {
    Integer width;
    Integer height;
    Integer x;
    Integer y;
}
