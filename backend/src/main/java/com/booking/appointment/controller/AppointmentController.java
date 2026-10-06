package com.booking.appointment.controller;

import com.booking.appointment.dto.AppointmentResponse;
import com.booking.appointment.dto.BookAppointmentRequest;
import com.booking.appointment.dto.UserAppointmentResponse;
import com.booking.appointment.service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @Valid @RequestBody BookAppointmentRequest request,
            HttpServletRequest httpRequest
    ) {
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        AppointmentResponse response = appointmentService.bookAppointment(request, userId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<List<UserAppointmentResponse>> getMyAppointments(HttpServletRequest httpRequest) {
        UUID userId = (UUID) httpRequest.getAttribute("userId");
        List<UserAppointmentResponse> appointments = appointmentService.getMyAppointments(userId);
        return ResponseEntity.ok(appointments);
    }
}
