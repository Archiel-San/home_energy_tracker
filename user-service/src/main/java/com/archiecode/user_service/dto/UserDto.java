package com.archiecode.user_service.dto;

import com.archiecode.user_service.entity.Usuario;
import lombok.Builder;

@Builder
public record UserDto(
        String name,
        String surname,
        String email,
        String address,
        Boolean alerting,
        double energyAlertingThreshold
) {

    public UserDto(Usuario usuario){
        this(usuario.getName(), usuario.getSurname(), usuario.getEmail(), usuario.getAddress(), usuario.isAlerting(), usuario.getEnergy_alerting_threshold());
    }

}
