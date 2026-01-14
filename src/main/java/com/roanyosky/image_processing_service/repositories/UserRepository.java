package com.roanyosky.image_processing_service.repositories;

import com.roanyosky.image_processing_service.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User,Integer> {

    User findByUsername(String username);
}
