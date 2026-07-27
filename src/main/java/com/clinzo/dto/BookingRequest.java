package com.clinzo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record BookingRequest(
        @NotBlank(message = "Patient name is required") String patientName,
        @NotBlank(message = "Patient email is required") @Email(message = "Email must be valid") String patientEmail) {
}
