package com.clinzo.dto;

import com.clinzo.entity.BookingStatus;

import java.time.Instant;
import java.util.UUID;

public record BookingResponse(UUID id,
                              UUID slotId,
                              String patientName,
                              String patientEmail,
                              BookingStatus bookingStatus,
                              Instant createdAt) {
}
