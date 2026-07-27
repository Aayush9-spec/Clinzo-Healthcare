package com.clinzo.mapper;

import com.clinzo.dto.DoctorResponse;
import com.clinzo.entity.Doctor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", config = MapStructConfig.class)
public interface DoctorMapper {
    DoctorResponse toResponse(Doctor doctor);
}
