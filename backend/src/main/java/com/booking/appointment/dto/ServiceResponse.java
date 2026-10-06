package com.booking.appointment.dto;

import com.booking.appointment.model.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {
    private UUID id;
    private String name;
    private ServiceType type;
    private Integer durationMinutes;
    private String providerName;
}