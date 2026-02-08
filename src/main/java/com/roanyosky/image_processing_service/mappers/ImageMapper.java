package com.roanyosky.image_processing_service.mappers;

import com.roanyosky.image_processing_service.dtos.ImageCreateDto;
import com.roanyosky.image_processing_service.dtos.ImageDto;
import com.roanyosky.image_processing_service.dtos.ImageUpdateDto;
import com.roanyosky.image_processing_service.entities.Image;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface ImageMapper {
    @Mapping(target = "owner_id", source = "owner.id")
    ImageDto toDto(Image image);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner.id", source = "owner_id")
    @Mapping(target = "r2Key", source = "r2Key")
    Image toEntity(ImageCreateDto imageCreateDto);

    void updateDto(ImageUpdateDto imageUpdateDto, @MappingTarget Image image);
}
