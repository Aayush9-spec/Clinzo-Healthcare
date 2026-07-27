package com.clinzo.dto;

import java.util.UUID;

public record DoctorResponse(UUID id, String name, String timezone, Integer slotDuration, Integer bufferTime) {
}
