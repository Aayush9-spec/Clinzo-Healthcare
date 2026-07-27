package com.clinzo.service;

import com.clinzo.dto.BookingRequest;
import com.clinzo.dto.BookingResponse;
import com.clinzo.dto.RescheduleRequest;
import com.clinzo.entity.Booking;
import com.clinzo.entity.BookingStatus;
import com.clinzo.entity.Slot;
import com.clinzo.entity.SlotStatus;
import com.clinzo.exception.BadRequestException;
import com.clinzo.exception.ConflictException;
import com.clinzo.exception.ResourceNotFoundException;
import com.clinzo.mapper.BookingMapper;
import com.clinzo.repository.BookingRepository;
import com.clinzo.repository.SlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BookingService {
    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final BookingMapper bookingMapper;
    private final AuditLogService auditLogService;

    public BookingService(BookingRepository bookingRepository,
                          SlotRepository slotRepository,
                          BookingMapper bookingMapper,
                          AuditLogService auditLogService) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.bookingMapper = bookingMapper;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public BookingResponse bookSlot(UUID slotId, BookingRequest request) {
        Slot slot = slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new ConflictException("Slot is no longer available");
        }
        slot.setStatus(SlotStatus.BOOKED);
        Booking booking = Booking.builder()
                .slot(slot)
                .patientName(request.patientName())
                .patientEmail(request.patientEmail())
                .bookingStatus(BookingStatus.BOOKED)
                .createdAt(Instant.now())
                .build();
        Booking saved = bookingRepository.save(booking);
        auditLogService.record("BOOK_SLOT", "Booking", saved.getId().toString(), "slotId=" + slotId);
        return bookingMapper.toResponse(saved);
    }

    @Transactional
    public BookingResponse cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Booking is already cancelled");
        }
        Slot slot = slotRepository.findByIdForUpdate(booking.getSlot().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
        slot.setStatus(SlotStatus.AVAILABLE);
        booking.setBookingStatus(BookingStatus.CANCELLED);
        auditLogService.record("CANCEL_BOOKING", "Booking", booking.getId().toString(), "slotId=" + slot.getId());
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResponse rescheduleBooking(UUID bookingId, RescheduleRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        if (booking.getBookingStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Cancelled bookings cannot be rescheduled");
        }

        UUID originalSlotId = booking.getSlot().getId();
        UUID targetSlotId = request.targetSlotId();
        if (originalSlotId.equals(targetSlotId)) {
            throw new BadRequestException("Target slot must differ from the current slot");
        }

        UUID firstLock = originalSlotId.compareTo(targetSlotId) < 0 ? originalSlotId : targetSlotId;
        UUID secondLock = originalSlotId.compareTo(targetSlotId) < 0 ? targetSlotId : originalSlotId;

        Slot firstSlot = slotRepository.findByIdForUpdate(firstLock)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));
        Slot secondSlot = slotRepository.findByIdForUpdate(secondLock)
                .orElseThrow(() -> new ResourceNotFoundException("Slot not found"));

        Slot oldSlot = firstSlot.getId().equals(originalSlotId) ? firstSlot : secondSlot;
        Slot newSlot = firstSlot.getId().equals(targetSlotId) ? firstSlot : secondSlot;

        if (newSlot.getStatus() != SlotStatus.AVAILABLE) {
            throw new ConflictException("Target slot is not available");
        }
        oldSlot.setStatus(SlotStatus.AVAILABLE);
        newSlot.setStatus(SlotStatus.BOOKED);
        booking.setSlot(newSlot);
        booking.setBookingStatus(BookingStatus.RESCHEDULED);
        auditLogService.record("RESCHEDULE_BOOKING", "Booking", booking.getId().toString(), "from=" + oldSlot.getId() + " to=" + newSlot.getId());
        return bookingMapper.toResponse(bookingRepository.save(booking));
    }
}
