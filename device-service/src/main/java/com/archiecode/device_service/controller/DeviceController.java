package com.archiecode.device_service.controller;

import com.archiecode.device_service.dto.DeviceDto;
import com.archiecode.device_service.service.DeviceService;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/device")
public class DeviceController {

    private final DeviceService deviceService;
    private DeviceController(DeviceService deviceService){
        this.deviceService = deviceService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDeviceById(@Param("id") @PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(deviceService.getDeviceById(id));
    }

    @PostMapping("")
    public ResponseEntity<?> createDevice(@RequestBody DeviceDto deviceDto){
        deviceService.createDevice(deviceDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Device Created Successfully");
    }


}
