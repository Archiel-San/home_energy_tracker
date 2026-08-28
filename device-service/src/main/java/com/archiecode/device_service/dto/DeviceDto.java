package com.archiecode.device_service.dto;

import com.archiecode.device_service.entity.Device;
import com.archiecode.device_service.enums.DeviceType;

public record DeviceDto(
        Long id,
        String name,
        DeviceType deviceType,
        String location,
        Long userId
) {

    public DeviceDto(Device device){
        this(device.getId(), device.getName(), device.getType(), device.getLocation(), device.getUserId());
    }
}
