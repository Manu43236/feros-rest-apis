package com.feros.api.service;

import com.feros.api.dto.request.AttendanceLocationRequest;
import com.feros.api.dto.response.AttendanceLocationResponse;

import java.util.List;

public interface AttendanceLocationService {
    List<AttendanceLocationResponse> getAll();
    AttendanceLocationResponse create(AttendanceLocationRequest request);
    AttendanceLocationResponse update(Long id, AttendanceLocationRequest request);
    void delete(Long id);
}
