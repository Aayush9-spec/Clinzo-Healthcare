package com.clinzo;

import com.clinzo.dto.AvailabilityRequest;
import com.clinzo.dto.BookingRequest;
import com.clinzo.dto.RescheduleRequest;
import com.clinzo.entity.Booking;
import com.clinzo.entity.BookingStatus;
import com.clinzo.entity.Doctor;
import com.clinzo.entity.Slot;
import com.clinzo.entity.SlotStatus;
import com.clinzo.repository.AvailabilityRepository;
import com.clinzo.repository.BookingRepository;
import com.clinzo.repository.DoctorRepository;
import com.clinzo.repository.SlotRepository;
import com.clinzo.service.AvailabilityService;
import com.clinzo.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class BookingIntegrationTest {

    @Autowired
    private AvailabilityService availabilityService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private SlotRepository slotRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void cleanDatabase() {
        bookingRepository.deleteAll();
        slotRepository.deleteAll();
        availabilityRepository.deleteAll();
        doctorRepository.deleteAll();
    }

    @Test
    void shouldOnlyAllowSingleBookingUnderConcurrentRequests() throws InterruptedException {
        Doctor doctor = doctorRepository.save(Doctor.builder()
                .name("Dr. Concurrent")
                .timezone("UTC")
                .slotDuration(15)
                .bufferTime(5)
                .build());

        LocalDate appointmentDate = LocalDate.now().plusDays(1);
        AvailabilityRequest request = new AvailabilityRequest(doctor.getId(), appointmentDate, LocalTime.of(9, 0), LocalTime.of(9, 30), appointmentDate.getDayOfWeek());
        var availabilityResponse = availabilityService.createAvailability(request);
        availabilityService.generateSlots(availabilityResponse.id());

        Slot slot = slotRepository.findAll().stream().filter(s -> s.getDoctor().getId().equals(doctor.getId())).findFirst().orElseThrow();
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<UUID>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            futures.add(executor.submit(() -> {
                start.await();
                try {
                    var booking = bookingService.bookSlot(slot.getId(), new BookingRequest("Patient", "patient@example.com"));
                    return booking.id();
                } catch (Exception ex) {
                    return null;
                }
            }));
        }

        start.countDown();
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        long successes = futures.stream().map(this::safeGet).filter(Objects::nonNull).count();
        assertThat(successes).isEqualTo(1);
        assertThat(bookingRepository.count()).isEqualTo(1);
        Slot refreshed = slotRepository.findById(slot.getId()).orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(SlotStatus.BOOKED);
    }

    @Test
    void shouldCancelAndRescheduleBooking() {
        Doctor doctor = doctorRepository.save(Doctor.builder()
                .name("Dr. Flow")
                .timezone("UTC")
                .slotDuration(15)
                .bufferTime(0)
                .build());

        LocalDate appointmentDate = LocalDate.now().plusDays(2);
        AvailabilityRequest request = new AvailabilityRequest(doctor.getId(), appointmentDate, LocalTime.of(10, 0), LocalTime.of(11, 0), appointmentDate.getDayOfWeek());
        var availabilityResponse = availabilityService.createAvailability(request);
        availabilityService.generateSlots(availabilityResponse.id());

        List<Slot> slots = slotRepository.findAll();
        assertThat(slots).hasSizeGreaterThanOrEqualTo(4);
        Slot first = slots.stream().filter(s -> s.getStatus() == SlotStatus.AVAILABLE).findFirst().orElseThrow();
        BookingRequest bookingRequest = new BookingRequest("Patient", "patient2@example.com");
        var booking = bookingService.bookSlot(first.getId(), bookingRequest);

        bookingService.cancelBooking(booking.id());
        Booking cancelled = bookingRepository.findById(booking.id()).orElseThrow();
        assertThat(cancelled.getBookingStatus()).isEqualTo(BookingStatus.CANCELLED);

        Slot availableSlot = slotRepository.findById(first.getId()).orElseThrow();
        assertThat(availableSlot.getStatus()).isEqualTo(SlotStatus.AVAILABLE);

        Slot nextAvailable = slotRepository.findAll().stream()
                .filter(s -> s.getStatus() == SlotStatus.AVAILABLE && !s.getId().equals(first.getId()))
                .findFirst().orElseThrow();
        var newBooking = bookingService.bookSlot(nextAvailable.getId(), bookingRequest);

        var rescheduleRequest = new RescheduleRequest(first.getId());
        var rescheduled = bookingService.rescheduleBooking(newBooking.id(), rescheduleRequest);
        assertThat(rescheduled.bookingStatus()).isEqualTo(BookingStatus.RESCHEDULED);
        assertThat(slotRepository.findById(nextAvailable.getId()).orElseThrow().getStatus()).isEqualTo(SlotStatus.AVAILABLE);
        assertThat(slotRepository.findById(first.getId()).orElseThrow().getStatus()).isEqualTo(SlotStatus.BOOKED);
    }

    private UUID safeGet(Future<UUID> future) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }
}
