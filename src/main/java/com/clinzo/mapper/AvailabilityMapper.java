package com.clinzo.mapper;

import com.clinzo.dto.AvailabilityResponse;
import com.clinzo.entity.Availability;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = MapStructConfig.class)
public interface AvailabilityMapper {
    @Mapping(target = "doctorId", source = "doctor.id")
    AvailabilityResponse toResponse(Availability availability);
}
