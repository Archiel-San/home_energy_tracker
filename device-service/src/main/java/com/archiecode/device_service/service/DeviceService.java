package com.archiecode.device_service.service;

import com.archiecode.device_service.dto.DeviceDto;
import com.archiecode.device_service.entity.Device;
import com.archiecode.device_service.exception.DeviceNotFoundException;
import com.archiecode.device_service.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository){
        this.deviceRepository = deviceRepository;
    }

    public DeviceDto getDeviceById(Long id){
        return deviceRepository
                .findById(id).map(DeviceDto::new)
                .orElseThrow(()-> new DeviceNotFoundException("Device not found for id: "+id));
    }

    public DeviceDto createDevice(DeviceDto deviceDto){
        Device d = new Device();
        d.setName(deviceDto.name());
        d.setType(deviceDto.deviceType());
        d.setLocation(deviceDto.location());
        d.setUserId(deviceDto.userId());
        deviceRepository.save(d);
        return new DeviceDto(d);
    }

}
