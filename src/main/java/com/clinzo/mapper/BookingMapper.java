package com.clinzo.mapper;

import com.clinzo.dto.BookingResponse;
import com.clinzo.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = MapStructConfig.class)
public interface BookingMapper {
    @Mapping(target = "slotId", source = "slot.id")
    BookingResponse toResponse(Booking booking);
}
