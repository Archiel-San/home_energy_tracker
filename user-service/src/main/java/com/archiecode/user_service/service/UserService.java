package com.archiecode.user_service.service;

import com.archiecode.user_service.dto.UserDto;
import com.archiecode.user_service.entity.Usuario;
import com.archiecode.user_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserService (UserRepository userRepository){
        this.userRepository = userRepository;
    }
    public UserDto createUser(UserDto userDto){
        log.info("Creating user: {}", userDto);

        final Usuario createdUser = Usuario.builder()
                .name(userDto.name())
                .name(userDto.surname())
                .email(userDto.email())
                .address(userDto.address())
                .alerting(userDto.alerting())
                .energy_alerting_threshold(userDto.energyAlertingThreshold())
                .build();

        final Usuario saved = userRepository.save(createdUser);
        return new UserDto(saved);
    }

    public UserDto getUserById(Long id){
        return userRepository.findById(id)
                .map(UserDto::new)
                .orElseThrow(() -> new RuntimeException("Usuario nao Encontrado"));
    }


    

}
