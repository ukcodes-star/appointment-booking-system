package com.booking.appointment.dto;

import com.booking.appointment.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private UUID id;
    private String slotId;
    private AppointmentStatus status;
}
