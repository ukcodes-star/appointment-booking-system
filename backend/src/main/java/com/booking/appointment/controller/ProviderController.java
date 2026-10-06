package com.booking.appointment.controller;

import com.booking.appointment.dto.ProviderServiceScheduleResponse;
import com.booking.appointment.service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/providers")
@CrossOrigin(origins = "*")
public class ProviderController {

    private final AppointmentService appointmentService;

    public ProviderController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/me/schedule")
    public ResponseEntity<List<ProviderServiceScheduleResponse>> getProviderSchedule(
            @RequestParam String date,
            HttpServletRequest httpRequest
    ) {
        UUID providerId = (UUID) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        List<ProviderServiceScheduleResponse> schedule = appointmentService.getProviderSchedule(providerId, role, date);
        return ResponseEntity.ok(schedule);
    }
}
