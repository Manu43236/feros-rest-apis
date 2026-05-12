package com.feros.api.service;

import com.feros.api.dto.request.AttendanceRequest;
import com.feros.api.dto.request.BulkAttendanceRequest;
import com.feros.api.dto.request.MarkMobilePresentRequest;
import com.feros.api.dto.request.MarkOwnAttendanceRequest;
import com.feros.api.dto.request.ReviewTripProofRequest;
import com.feros.api.dto.request.TripProofRequest;
import com.feros.api.dto.response.AttendanceResponse;
import com.feros.api.dto.response.TripProofResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceResponse markAttendance(AttendanceRequest request);

    AttendanceResponse markOwnAttendance(MarkOwnAttendanceRequest request);

    List<AttendanceResponse> markBulkAttendance(BulkAttendanceRequest request);

    List<AttendanceResponse> getAttendanceByDate(LocalDate date);

    List<AttendanceResponse> getAttendanceByUser(Long userId, LocalDate from, LocalDate to);

    AttendanceResponse updateAttendance(Long id, AttendanceRequest request);

    AttendanceResponse approveAttendance(Long id);

    AttendanceResponse rejectAttendance(Long id);

    List<AttendanceResponse> getPendingAttendance();

    List<AttendanceResponse> getMyAttendance(LocalDate from, LocalDate to);

    AttendanceResponse markMobilePresent(MarkMobilePresentRequest request);

    AttendanceResponse getTodayAttendanceStatus();

    TripProofResponse addTripProof(Long userId, TripProofRequest request);

    List<TripProofResponse> getTripProofsByLr(Long lrId);

    List<TripProofResponse> getTripProofsByUser(Long userId);

    TripProofResponse reviewTripProof(Long proofId, ReviewTripProofRequest request);
}