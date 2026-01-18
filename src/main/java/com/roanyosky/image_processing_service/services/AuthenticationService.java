package com.roanyosky.image_processing_service.services;

import com.roanyosky.image_processing_service.dtos.AuthenticationResponseDto;
import com.roanyosky.image_processing_service.dtos.LoginDto;
import com.roanyosky.image_processing_service.dtos.UserCreateDto;
import com.roanyosky.image_processing_service.dtos.UserDto;
import com.roanyosky.image_processing_service.entities.User;
import com.roanyosky.image_processing_service.mappers.UserMapper;
import com.roanyosky.image_processing_service.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    public AuthenticationResponseDto register(UserCreateDto userCreateDto){
        //Creates a user
        User user = userMapper.toEntity(userCreateDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        //Gera um token
        String jwtToken = jwtService.generateToken(user);
        UserDto userDto = userMapper.toDto(user);

        return AuthenticationResponseDto.builder()
                .token(jwtToken)
                .userDto(userDto)
                .build();
    }


    public AuthenticationResponseDto autheticate(LoginDto loginDto){
        // This will throw an exception if the password/username is wrong
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getUsername(), loginDto.getPassword()));

        User user = userRepository.findByUsername(loginDto.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

        //Gererate the token
        String jwtToken = jwtService.generateToken(user);
        UserDto userDto = userMapper.toDto(user);

        return AuthenticationResponseDto.builder()
                .token(jwtToken)
                .userDto(userDto)
                .build();
    }
}
