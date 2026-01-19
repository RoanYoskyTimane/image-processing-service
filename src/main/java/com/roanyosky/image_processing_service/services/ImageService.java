package com.roanyosky.image_processing_service.services;

import com.roanyosky.image_processing_service.dtos.ImageCreateDto;
import com.roanyosky.image_processing_service.dtos.ImageDto;
import com.roanyosky.image_processing_service.dtos.ImageUpdateDto;
import com.roanyosky.image_processing_service.entities.Image;
import com.roanyosky.image_processing_service.mappers.ImageMapper;
import com.roanyosky.image_processing_service.repositories.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;

    public List<ImageDto> getAllImages(){
        return imageRepository.findAll().stream().map(imageMapper::toDto).collect(Collectors.toList());
    }

    public ImageDto getImageById(UUID id){
        Image image = imageRepository.findById(id).orElseThrow(()->new RuntimeException("A imagem nao foi encontrada"+id));
        return imageMapper.toDto(image);
    }

    public ImageDto createImage(ImageCreateDto imageCreateDto){
        Image image = imageMapper.toEntity(imageCreateDto);
        imageRepository.save(image);

        return imageMapper.toDto(image);
    }

    public ImageDto updateImage(UUID id,ImageUpdateDto imageUpdateDto){
        Image image = imageRepository.findById(id).orElseThrow(()->new RuntimeException("A imagem nao foi encontrada"+id));
        imageMapper.updateDto(imageUpdateDto, image);
        return imageMapper.toDto(imageRepository.save(image));
    }

    public void deleteImage(UUID id){
        Image image = imageRepository.findById(id).orElseThrow(()-> new RuntimeException("A imagem nao foi encontrada"+id));
        imageRepository.delete(image);
    }
}
