package com.clinzo.dto;

import com.clinzo.entity.ReservationStatus;

import java.time.ZonedDateTime;
import java.util.UUID;

public record ReservationResponse(UUID id,
                                  UUID slotId,
                                  ZonedDateTime expiresAt,
                                  ReservationStatus status) {
}
