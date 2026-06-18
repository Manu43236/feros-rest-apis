package com.feros.api.service.impl;

import com.feros.api.dto.request.AttendanceLocationRequest;
import com.feros.api.dto.response.AttendanceLocationResponse;
import com.feros.api.entity.AttendanceLocation;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.AttendanceLocationRepository;
import com.feros.api.service.AttendanceLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceLocationServiceImpl implements AttendanceLocationService {

    private final AttendanceLocationRepository attendanceLocationRepository;

    @Override
    public List<AttendanceLocationResponse> getAll() {
        return attendanceLocationRepository.findAll()
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public AttendanceLocationResponse create(AttendanceLocationRequest request) {
        AttendanceLocation location = AttendanceLocation.builder()
                .name(request.getName())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .radiusMeters(request.getRadiusMeters())
                .isActive(true)
                .build();
        return mapToResponse(attendanceLocationRepository.save(location));
    }

    @Override
    @Transactional
    public AttendanceLocationResponse update(Long id, AttendanceLocationRequest request) {
        AttendanceLocation location = attendanceLocationRepository.findById(id)
                .orElseThrow(() -> new FerosException("Attendance location not found", HttpStatus.NOT_FOUND));
        location.setName(request.getName());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setRadiusMeters(request.getRadiusMeters());
        return mapToResponse(attendanceLocationRepository.save(location));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AttendanceLocation location = attendanceLocationRepository.findById(id)
                .orElseThrow(() -> new FerosException("Attendance location not found", HttpStatus.NOT_FOUND));
        location.setIsActive(false);
        attendanceLocationRepository.save(location);
    }

    private AttendanceLocationResponse mapToResponse(AttendanceLocation l) {
        return AttendanceLocationResponse.builder()
                .id(l.getId())
                .name(l.getName())
                .latitude(l.getLatitude())
                .longitude(l.getLongitude())
                .radiusMeters(l.getRadiusMeters())
                .isActive(l.getIsActive())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }
}
