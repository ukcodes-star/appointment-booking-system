package com.booking.appointment.dto;

import com.booking.appointment.model.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderServiceScheduleResponse {
    private UUID serviceId;
    private String serviceName;
    private ServiceType type;
    private List<ProviderAppointmentItem> appointments;
}
