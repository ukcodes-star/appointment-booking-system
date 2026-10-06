package com.booking.appointment.dto;

import com.booking.appointment.model.ServiceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateServiceRequest {
    @NotBlank(message = "Service name is required")
    private String name;

    @NotNull(message = "Service type is required")
    private ServiceType type;

    @NotNull(message = "Duration in minutes is required")
    @Min(value = 30, message = "Minimum duration is 30 minutes")
    @Max(value = 120, message = "Maximum duration is 120 minutes")
    private Integer durationMinutes;
}