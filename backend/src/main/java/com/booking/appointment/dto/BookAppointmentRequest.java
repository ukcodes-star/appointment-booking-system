package com.booking.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookAppointmentRequest {
    @NotBlank(message = "slotId is required")
    private String slotId;
}
