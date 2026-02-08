package com.roanyosky.image_processing_service.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginDto {
    @Email(message="Email cannot be null")
    @NotNull
    private String username;

    @NotNull
    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters.")
    private String password;
}
