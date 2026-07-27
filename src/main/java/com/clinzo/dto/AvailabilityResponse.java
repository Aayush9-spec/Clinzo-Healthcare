package com.clinzo.dto;

import com.clinzo.entity.AvailabilityStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AvailabilityResponse(UUID id,
                                   UUID doctorId,
                                   LocalDate date,
                                   LocalTime startTime,
                                   LocalTime endTime,
                                   DayOfWeek recurringWeekday,
                                   AvailabilityStatus status) {
}
