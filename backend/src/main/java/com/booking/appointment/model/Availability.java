package com.booking.appointment.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "availabilities", indexes = {
        @Index(name = "idx_avail_service_day", columnList = "service_id, dayOfWeek")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceEntity service;

    @Column(nullable = false)
    private Integer dayOfWeek; // 0 = Sunday, 6 = Saturday

    @Column(nullable = false, length = 5)
    private String startTime; // "HH:MM"

    @Column(nullable = false, length = 5)
    private String endTime; // "HH:MM"
}