package com.clinzo.dto;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(Instant timestamp, int status, String error, List<String> messages, String path) {
}
