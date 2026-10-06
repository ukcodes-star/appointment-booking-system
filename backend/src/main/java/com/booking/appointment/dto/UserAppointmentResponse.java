package com.booking.appointment.dto;

import com.booking.appointment.model.AppointmentStatus;
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
public class UserAppointmentResponse {
    private UUID id;
    private String serviceName;
    private ServiceType type;
    private String date;
    private String startTime;
    private String endTime;
    private AppointmentStatus status;
}
