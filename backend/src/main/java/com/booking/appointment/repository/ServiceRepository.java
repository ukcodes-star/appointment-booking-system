package com.booking.appointment.repository;

import com.booking.appointment.model.ServiceEntity;
import com.booking.appointment.model.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, UUID> {
    List<ServiceEntity> findByType(ServiceType type);
    List<ServiceEntity> findByProviderId(UUID providerId);
}