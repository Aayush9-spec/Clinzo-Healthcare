package com.clinzo.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record RescheduleRequest(@NotNull(message = "Target slot id is required") UUID targetSlotId) {
}
