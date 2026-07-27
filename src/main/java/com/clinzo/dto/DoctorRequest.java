package com.clinzo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record DoctorRequest(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Timezone is required") @Pattern(regexp = "^[A-Za-z_]+/[A-Za-z_]+$", message = "Timezone must be valid zone id") String timezone,
        @NotNull(message = "Slot duration is required") @Min(value = 1, message = "Slot duration must be greater than 0") Integer slotDuration,
        @NotNull(message = "Buffer time is required") @Min(value = 0, message = "Buffer time must be zero or positive") Integer bufferTime) {
}
