package com.booking.appointment.service;

import com.booking.appointment.dto.ServiceSlotsResponse;
import com.booking.appointment.dto.SlotItem;
import com.booking.appointment.model.Appointment;
import com.booking.appointment.model.AppointmentStatus;
import com.booking.appointment.model.Availability;
import com.booking.appointment.model.ServiceEntity;
import com.booking.appointment.repository.AppointmentRepository;
import com.booking.appointment.repository.AvailabilityRepository;
import com.booking.appointment.repository.ServiceRepository;
import com.booking.appointment.util.TimeUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class SlotService {

    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");

    private final ServiceRepository serviceRepository;
    private final AvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;

    public SlotService(
            ServiceRepository serviceRepository,
            AvailabilityRepository availabilityRepository,
            AppointmentRepository appointmentRepository
    ) {
        this.serviceRepository = serviceRepository;
        this.availabilityRepository = availabilityRepository;
        this.appointmentRepository = appointmentRepository;
    }

    public ServiceSlotsResponse getSlotsForService(UUID serviceId, String dateStr) {
        LocalDate date = validateAndParseDate(dateStr);

        ServiceEntity service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Service not found"));

        int dayOfWeek = date.getDayOfWeek().getValue() % 7;
        List<Availability> availabilities = availabilityRepository.findByServiceIdAndDayOfWeek(serviceId, dayOfWeek);
        availabilities.sort(Comparator.comparingInt(a -> TimeUtil.toMinutes(a.getStartTime())));

        List<Appointment> bookedList = appointmentRepository.findByServiceIdAndDateAndStatus(
                serviceId, dateStr, AppointmentStatus.BOOKED);

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        int currentMinutes = now.getHour() * 60 + now.getMinute();

        List<SlotItem> slots = new ArrayList<>();
        int duration = service.getDurationMinutes();

        for (Availability avail : availabilities) {
            int windowStart = TimeUtil.toMinutes(avail.getStartTime());
            int windowEnd = TimeUtil.toMinutes(avail.getEndTime());

            for (int slotStart = windowStart; slotStart + duration <= windowEnd; slotStart += duration) {
                int slotEnd = slotStart + duration;

                if (date.isEqual(today) && slotStart <= currentMinutes) {
                    continue;
                }

                boolean overlaps = false;
                for (Appointment app : bookedList) {
                    int appStart = TimeUtil.toMinutes(app.getStartTime());
                    int appEnd = TimeUtil.toMinutes(app.getEndTime());
                    if (TimeUtil.hasOverlap(slotStart, slotEnd, appStart, appEnd)) {
                        overlaps = true;
                        break;
                    }
                }

                if (!overlaps) {
                    String startStr = TimeUtil.toTimeString(slotStart);
                    String endStr = TimeUtil.toTimeString(slotEnd);
                    String slotId = serviceId + "_" + dateStr + "_" + startStr;

                    slots.add(SlotItem.builder()
                            .slotId(slotId)
                            .startTime(startStr)
                            .endTime(endStr)
                            .build());
                }
            }
        }

        return ServiceSlotsResponse.builder()
                .serviceId(serviceId)
                .date(dateStr)
                .slots(slots)
                .build();
    }

    public LocalDate validateAndParseDate(String dateStr) {
        if (dateStr == null || !DATE_PATTERN.matcher(dateStr).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date or format");
        }
        LocalDate date;
        try {
            date = LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date or format");
        }
        if (date.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date or format: Date is in the past");
        }
        return date;
    }
}
