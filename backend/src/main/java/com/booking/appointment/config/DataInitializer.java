package com.booking.appointment.config;

import com.booking.appointment.model.*;
import com.booking.appointment.repository.AvailabilityRepository;
import com.booking.appointment.repository.ServiceRepository;
import com.booking.appointment.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final AvailabilityRepository availabilityRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            ServiceRepository serviceRepository,
            AvailabilityRepository availabilityRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
        this.availabilityRepository = availabilityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed Doctor Provider
        if (!userRepository.existsByEmail("doctor@clinic.com")) {
            User doctor = userRepository.save(User.builder()
                    .name("Dr. Sarah Smith")
                    .email("doctor@clinic.com")
                    .passwordHash(passwordEncoder.encode("Password123"))
                    .role(Role.SERVICE_PROVIDER)
                    .build());

            ServiceEntity medicalService = serviceRepository.save(ServiceEntity.builder()
                    .name("General Health Consultation")
                    .type(ServiceType.MEDICAL)
                    .durationMinutes(30)
                    .provider(doctor)
                    .build());

            // Monday (1) to Friday (5) availability
            for (int day = 1; day <= 5; day++) {
                availabilityRepository.save(Availability.builder()
                        .service(medicalService)
                        .dayOfWeek(day)
                        .startTime("09:00")
                        .endTime("17:00")
                        .build());
            }
        }

        // Seed Fitness/Beauty Provider
        if (!userRepository.existsByEmail("trainer@gym.com")) {
            User trainer = userRepository.save(User.builder()
                    .name("Alex Rivers")
                    .email("trainer@gym.com")
                    .passwordHash(passwordEncoder.encode("Password123"))
                    .role(Role.SERVICE_PROVIDER)
                    .build());

            ServiceEntity fitnessService = serviceRepository.save(ServiceEntity.builder()
                    .name("Personal Fitness Training")
                    .type(ServiceType.FITNESS)
                    .durationMinutes(60)
                    .provider(trainer)
                    .build());

            for (int day = 1; day <= 6; day++) {
                availabilityRepository.save(Availability.builder()
                        .service(fitnessService)
                        .dayOfWeek(day)
                        .startTime("08:00")
                        .endTime("16:00")
                        .build());
            }
        }

        // Seed Regular User
        if (!userRepository.existsByEmail("user@example.com")) {
            userRepository.save(User.builder()
                    .name("John Doe")
                    .email("user@example.com")
                    .passwordHash(passwordEncoder.encode("Password123"))
                    .role(Role.USER)
                    .build());
        }
    }
}
