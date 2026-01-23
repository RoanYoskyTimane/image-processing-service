package com.roanyosky.image_processing_service.repositories;

import com.roanyosky.image_processing_service.entities.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {
    List<Image> findImageByOwnerId(Integer ownerId, Pageable pageable);
}
