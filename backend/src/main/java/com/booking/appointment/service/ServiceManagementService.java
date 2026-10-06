package com.booking.appointment.service;

import com.booking.appointment.dto.CreateServiceRequest;
import com.booking.appointment.dto.ServiceResponse;
import com.booking.appointment.dto.SetAvailabilityRequest;
import com.booking.appointment.model.Availability;
import com.booking.appointment.model.ServiceEntity;
import com.booking.appointment.model.ServiceType;
import com.booking.appointment.model.User;
import com.booking.appointment.repository.AvailabilityRepository;
import com.booking.appointment.repository.ServiceRepository;
import com.booking.appointment.repository.UserRepository;
import com.booking.appointment.util.TimeUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ServiceManagementService {

    private final ServiceRepository serviceRepository;
    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    public ServiceManagementService(
            ServiceRepository serviceRepository,
            AvailabilityRepository availabilityRepository,
            UserRepository userRepository) {
        this.serviceRepository = serviceRepository;
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ServiceResponse createService(CreateServiceRequest request, UUID providerId, String role) {
        if (!"SERVICE_PROVIDER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only service providers can create services");
        }

        if (request.getDurationMinutes() % 30 != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duration must be a multiple of 30 minutes");
        }

        User provider = userRepository.findById(providerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Provider not found"));

        ServiceEntity service = ServiceEntity.builder()
                .name(request.getName())
                .type(request.getType())
                .durationMinutes(request.getDurationMinutes())
                .provider(provider)
                .build();

        ServiceEntity saved = serviceRepository.save(service);

        return ServiceResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .type(saved.getType())
                .durationMinutes(saved.getDurationMinutes())
                .providerName(provider.getName())
                .build();
    }

    @Transactional
    public void setAvailability(UUID serviceId, SetAvailabilityRequest request, UUID providerId) {
        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        if (!service.getProvider().getId().equals(providerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Service does not belong to provider");
        }

        int newStart = TimeUtil.toMinutes(request.getStartTime());
        int newEnd = TimeUtil.toMinutes(request.getEndTime());

        if (newStart >= newEnd) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startTime must be before endTime");
        }

        List<Availability> existing = availabilityRepository.findByServiceIdAndDayOfWeek(serviceId, request.getDayOfWeek());
        for (Availability a : existing) {
            int existingStart = TimeUtil.toMinutes(a.getStartTime());
            int existingEnd = TimeUtil.toMinutes(a.getEndTime());
            if (TimeUtil.hasOverlap(newStart, newEnd, existingStart, existingEnd)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Overlapping availability window exists");
            }
        }

        Availability availability = Availability.builder()
                .service(service)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        availabilityRepository.save(availability);
    }

    public List<ServiceResponse> getServices(ServiceType type) {
        List<ServiceEntity> services = (type == null)
                ? serviceRepository.findAll()
                : serviceRepository.findByType(type);

        return services.stream()
                .map(s -> ServiceResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .type(s.getType())
                        .durationMinutes(s.getDurationMinutes())
                        .providerName(s.getProvider().getName())
                        .build())
                .collect(Collectors.toList());
    }
}