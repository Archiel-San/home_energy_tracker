package com.archiecode.device_service.entity;

import com.archiecode.device_service.enums.DeviceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "device")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private DeviceType type;

    private String location;

    @Column(name = "user_id")
    private Long userId;

}
