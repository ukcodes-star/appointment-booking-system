package com.booking.appointment.controller;

import com.booking.appointment.dto.CreateServiceRequest;
import com.booking.appointment.dto.ServiceResponse;
import com.booking.appointment.dto.ServiceSlotsResponse;
import com.booking.appointment.dto.SetAvailabilityRequest;
import com.booking.appointment.model.ServiceType;
import com.booking.appointment.service.ServiceManagementService;
import com.booking.appointment.service.SlotService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/services")
@CrossOrigin(origins = "*")
public class ServiceController {

    private final ServiceManagementService serviceManagementService;
    private final SlotService slotService;

    public ServiceController(
            ServiceManagementService serviceManagementService,
            SlotService slotService
    ) {
        this.serviceManagementService = serviceManagementService;
        this.slotService = slotService;
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> createService(
            @Valid @RequestBody CreateServiceRequest request,
            HttpServletRequest httpRequest
    ) {
        UUID providerId = (UUID) httpRequest.getAttribute("userId");
        String role = (String) httpRequest.getAttribute("role");
        ServiceResponse response = serviceManagementService.createService(request, providerId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{serviceId}/availability")
    public ResponseEntity<Void> setAvailability(
            @PathVariable UUID serviceId,
            @Valid @RequestBody SetAvailabilityRequest request,
            HttpServletRequest httpRequest
    ) {
        String role = (String) httpRequest.getAttribute("role");
        if (!"SERVICE_PROVIDER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only service providers can set availability");
        }
        UUID providerId = (UUID) httpRequest.getAttribute("userId");
        serviceManagementService.setAvailability(serviceId, request, providerId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> getServices(
            @RequestParam(required = false) ServiceType type
    ) {
        List<ServiceResponse> services = serviceManagementService.getServices(type);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{serviceId}/slots")
    public ResponseEntity<ServiceSlotsResponse> getSlots(
            @PathVariable UUID serviceId,
            @RequestParam String date
    ) {
        ServiceSlotsResponse response = slotService.getSlotsForService(serviceId, date);
        return ResponseEntity.ok(response);
    }
}