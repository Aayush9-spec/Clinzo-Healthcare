package com.clinzo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AvailabilityRequest(
        @NotNull(message = "Doctor id is required") UUID doctorId,
        @NotNull(message = "Date is required") LocalDate date,
        @NotNull(message = "Start time is required") LocalTime startTime,
        @NotNull(message = "End time is required") LocalTime endTime,
        DayOfWeek recurringWeekday) {
}
