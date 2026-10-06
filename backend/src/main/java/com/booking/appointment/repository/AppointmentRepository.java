package com.booking.appointment.repository;

import com.booking.appointment.model.Appointment;
import com.booking.appointment.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    boolean existsBySlotIdAndStatus(String slotId, AppointmentStatus status);
    Optional<Appointment> findBySlotId(String slotId);
    List<Appointment> findByServiceIdAndDateAndStatus(UUID serviceId, String date, AppointmentStatus status);
    List<Appointment> findByUserIdOrderByCreatedAtDesc(UUID userId);
    List<Appointment> findByServiceIdInAndDateAndStatusOrderByStartTimeAsc(List<UUID> serviceIds, String date, AppointmentStatus status);
}