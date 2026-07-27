package com.clinzo.util;

import java.time.*;
import java.time.format.DateTimeParseException;

public final class DateTimeUtil {
    private DateTimeUtil() {
    }

    public static ZonedDateTime toDoctorZone(Instant instant, String doctorTimezone) {
        ZoneId zoneId = ZoneId.of(doctorTimezone);
        return instant.atZone(zoneId);
    }

    public static Instant toUtcInstant(LocalDate date, LocalTime localTime, String zoneId) {
        return ZonedDateTime.of(date, localTime, ZoneId.of(zoneId)).toInstant();
    }

    public static ZonedDateTime toUtcZonedDateTime(Instant instant) {
        return instant.atZone(ZoneOffset.UTC);
    }

    public static boolean isFuture(LocalDate date) {
        return !date.isBefore(LocalDate.now(ZoneOffset.UTC));
    }

    public static boolean isValidTimezone(String timezone) {
        try {
            ZoneId.of(timezone);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }
}
