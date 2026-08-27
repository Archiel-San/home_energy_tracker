package com.archiecode.user_service.controller;

import com.archiecode.user_service.dto.UserDto;
import com.archiecode.user_service.service.UserService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService=userService;
    }

    @PostMapping()
    public ResponseEntity<UserDto> createuser(@RequestBody UserDto userDto){
        UserDto created = userService.createUser(userDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@Param("id") @PathVariable Long id){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(userService.getUserById(id));
        }
        catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuario Nao Encontrado");
        }
    }



}
