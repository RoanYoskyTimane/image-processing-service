package com.roanyosky.image_processing_service.mappers;

import com.roanyosky.image_processing_service.dtos.UserCreateDto;
import com.roanyosky.image_processing_service.dtos.UserDto;
import com.roanyosky.image_processing_service.dtos.UserUpdateDto;
import com.roanyosky.image_processing_service.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    User toEntity(UserCreateDto userCreateDto);

    void updateDto(UserUpdateDto userUpdateDto, @MappingTarget User user);
}
