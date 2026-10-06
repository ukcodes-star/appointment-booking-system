package com.booking.appointment.service;

import com.booking.appointment.dto.*;
import com.booking.appointment.model.Appointment;
import com.booking.appointment.model.AppointmentStatus;
import com.booking.appointment.model.Availability;
import com.booking.appointment.model.ServiceEntity;
import com.booking.appointment.model.User;
import com.booking.appointment.repository.AppointmentRepository;
import com.booking.appointment.repository.AvailabilityRepository;
import com.booking.appointment.repository.ServiceRepository;
import com.booking.appointment.repository.UserRepository;
import com.booking.appointment.util.TimeUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private static final Pattern TIME_PATTERN = Pattern.compile("^(0[0-9]|1[0-9]|2[0-3]):(00|30)$");
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private final AppointmentRepository appointmentRepository;
    private final ServiceRepository serviceRepository;
    private final AvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            ServiceRepository serviceRepository,
            AvailabilityRepository availabilityRepository,
            UserRepository userRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.serviceRepository = serviceRepository;
        this.availabilityRepository = availabilityRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AppointmentResponse bookAppointment(BookAppointmentRequest request, UUID userId, String role) {
        if (!"USER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only users can book appointments");
        }

        if (request.getSlotId() == null || request.getSlotId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "slotId is required");
        }

        String[] parts = request.getSlotId().split("_");
        if (parts.length != 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid slotId format");
        }

        UUID serviceId;
        try {
            serviceId = UUID.fromString(parts[0]);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid slotId: invalid service ID");
        }

        String dateStr = parts[1];
        String startTimeStr = parts[2];

        if (!TIME_PATTERN.matcher(startTimeStr).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid slotId or time: invalid time format");
        }

        if (!DATE_PATTERN.matcher(dateStr).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid slotId or time: invalid date format");
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid slotId or time: invalid date");
        }

        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking past dates or times is forbidden");
        }

        int startMinutes = TimeUtil.toMinutes(startTimeStr);
        if (date.isEqual(today)) {
            LocalTime now = LocalTime.now();
            int currentMinutes = now.getHour() * 60 + now.getMinute();
            if (startMinutes <= currentMinutes) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking past dates or times is forbidden");
            }
        }

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        if (service.getProvider().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Service Provider cannot book its own service");
        }

        int duration = service.getDurationMinutes();
        int endMinutes = startMinutes + duration;
        int dayOfWeek = date.getDayOfWeek().getValue() % 7;

        List<Availability> availabilities = availabilityRepository.findByServiceIdAndDayOfWeek(serviceId, dayOfWeek);
        boolean fitsAvailability = false;
        for (Availability avail : availabilities) {
            int availStart = TimeUtil.toMinutes(avail.getStartTime());
            int availEnd = TimeUtil.toMinutes(avail.getEndTime());
            if (startMinutes >= availStart && endMinutes <= availEnd) {
                if ((startMinutes - availStart) % duration == 0) {
                    fitsAvailability = true;
                    break;
                }
            }
        }

        if (!fitsAvailability) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Slot does not fall within provider availability");
        }

        if (appointmentRepository.existsBySlotIdAndStatus(request.getSlotId(), AppointmentStatus.BOOKED)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot already booked");
        }

        List<Appointment> existingAppointments = appointmentRepository.findByServiceIdAndDateAndStatus(
                serviceId, dateStr, AppointmentStatus.BOOKED);
        for (Appointment existing : existingAppointments) {
            int existStart = TimeUtil.toMinutes(existing.getStartTime());
            int existEnd = TimeUtil.toMinutes(existing.getEndTime());
            if (TimeUtil.hasOverlap(startMinutes, endMinutes, existStart, existEnd)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot already booked");
            }
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Appointment appointment = Appointment.builder()
                .user(user)
                .service(service)
                .date(dateStr)
                .startTime(startTimeStr)
                .endTime(TimeUtil.toTimeString(endMinutes))
                .slotId(request.getSlotId())
                .status(AppointmentStatus.BOOKED)
                .build();

        try {
            Appointment saved = appointmentRepository.save(appointment);
            return AppointmentResponse.builder()
                    .id(saved.getId())
                    .slotId(saved.getSlotId())
                    .status(saved.getStatus())
                    .build();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slot already booked");
        }
    }

    public List<UserAppointmentResponse> getMyAppointments(UUID userId) {
        List<Appointment> list = appointmentRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return list.stream()
                .map(a -> UserAppointmentResponse.builder()
                        .id(a.getId())
                        .serviceName(a.getService().getName())
                        .type(a.getService().getType())
                        .date(a.getDate())
                        .startTime(a.getStartTime())
                        .endTime(a.getEndTime())
                        .status(a.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    public List<ProviderServiceScheduleResponse> getProviderSchedule(UUID providerId, String role, String dateStr) {
        if (!"SERVICE_PROVIDER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only service providers can view schedule");
        }

        if (dateStr == null || !DATE_PATTERN.matcher(dateStr).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format");
        }

        try {
            LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format");
        }

        List<ServiceEntity> services = serviceRepository.findByProviderId(providerId);
        List<ProviderServiceScheduleResponse> schedule = new ArrayList<>();

        for (ServiceEntity service : services) {
            List<Appointment> appointments = appointmentRepository.findByServiceIdAndDateAndStatus(
                    service.getId(), dateStr, AppointmentStatus.BOOKED);
            appointments.sort(Comparator.comparingInt(a -> TimeUtil.toMinutes(a.getStartTime())));

            List<ProviderAppointmentItem> items = appointments.stream()
                    .map(a -> ProviderAppointmentItem.builder()
                            .id(a.getId())
                            .patientName(a.getUser().getName())
                            .date(a.getDate())
                            .startTime(a.getStartTime())
                            .endTime(a.getEndTime())
                            .slotId(a.getSlotId())
                            .status(a.getStatus())
                            .build())
                    .collect(Collectors.toList());

            schedule.add(ProviderServiceScheduleResponse.builder()
                    .serviceId(service.getId())
                    .serviceName(service.getName())
                    .type(service.getType())
                    .appointments(items)
                    .build());
        }

        return schedule;
    }
}
