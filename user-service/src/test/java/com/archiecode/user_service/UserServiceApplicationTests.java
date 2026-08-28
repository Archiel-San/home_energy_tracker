package com.archiecode.user_service;

import com.archiecode.user_service.entity.Usuario;
import com.archiecode.user_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class UserServiceApplicationTests {

    @Autowired
    private UserRepository userRepository;
    public static final int NUMBER_OF_USERS = 200;
    public static final int USERS = 10;


    @Test
	void contextLoads() {
	}

    @Disabled
    @Test
    void addUsersToDB() {
        for (int i = 1; i <= NUMBER_OF_USERS; i++) {
            Usuario user = Usuario.builder()
                    .name("User" + i)
                    .surname("Surname" + i)
                    .email("user" + i + "@example.com")
                    .address(i + " Example St")
                    .alerting(i % 2 == 0)
                    .energy_alerting_threshold(1000.0 + i)
                    .build();
            userRepository.save(user);
        }
        log.info("User Repository populated successfully");
    }

}

