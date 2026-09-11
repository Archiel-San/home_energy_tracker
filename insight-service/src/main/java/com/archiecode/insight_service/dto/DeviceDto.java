package com.archiecode.insight_service.dto;

public record DeviceDto(
        Long id,
        String name,
        String type,
        String location,
        double energyConsumed
) {
}
