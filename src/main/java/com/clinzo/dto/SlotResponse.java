package com.clinzo.dto;

import com.clinzo.entity.SlotStatus;

import java.time.ZonedDateTime;
import java.util.UUID;

public record SlotResponse(UUID id,
                            UUID doctorId,
                            UUID availabilityId,
                            ZonedDateTime startTime,
                            ZonedDateTime endTime,
                            SlotStatus status) {
}
