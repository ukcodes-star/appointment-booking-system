package com.booking.appointment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SetAvailabilityRequest {
    @NotNull(message = "Day of week is required")
    @Min(value = 0, message = "Day of week must be between 0 (Sunday) and 6 (Saturday)")
    @Max(value = 6, message = "Day of week must be between 0 (Sunday) and 6 (Saturday)")
    private Integer dayOfWeek;

    @NotBlank(message = "Start time is required")
    @Pattern(regexp = "^(0[0-9]|1[0-9]|2[0-3]):(00|30)$", message = "Time must be HH:MM in 24h format with minutes 00 or 30 only")
    private String startTime;

    @NotBlank(message = "End time is required")
    @Pattern(regexp = "^(0[0-9]|1[0-9]|2[0-3]):(00|30)$", message = "Time must be HH:MM in 24h format with minutes 00 or 30 only")
    private String endTime;
}