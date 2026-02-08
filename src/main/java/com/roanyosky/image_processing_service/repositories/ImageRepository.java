package com.roanyosky.image_processing_service.repositories;

import com.roanyosky.image_processing_service.entities.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {
    Page<Image> findImageByOwnerId(Integer ownerId, Pageable pageable);
}
