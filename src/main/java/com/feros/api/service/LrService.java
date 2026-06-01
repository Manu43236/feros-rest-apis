package com.feros.api.service;

import com.feros.api.dto.request.CreateLrRequest;
import com.feros.api.dto.request.LrChargeRequest;
import com.feros.api.dto.request.LrCheckpostRequest;
import com.feros.api.dto.request.UpdateLrRequest;
import com.feros.api.dto.response.LrChargeResponse;
import com.feros.api.dto.response.LrCheckpostResponse;
import com.feros.api.dto.response.LrResponse;
import com.feros.api.enums.LrStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface LrService {
    LrResponse createLr(CreateLrRequest request);
    LrResponse getLrById(Long id);
    Page<LrResponse> getAllLrs(int page, int size, String search, LrStatus status);
    List<LrResponse> getMyLrs();
    List<LrResponse> getLrsByOrder(Long orderId);
    LrResponse updateLr(Long id, UpdateLrRequest request);
    LrCheckpostResponse addCheckpost(Long lrId, LrCheckpostRequest request);
    List<LrCheckpostResponse> getCheckposts(Long lrId);
    void deleteCheckpost(Long checkpostId);
    LrChargeResponse addCharge(Long lrId, LrChargeRequest request);
    List<LrChargeResponse> getCharges(Long lrId);
    void deleteCharge(Long chargeId);
}