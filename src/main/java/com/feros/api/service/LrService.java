package com.feros.api.service;

import com.feros.api.dto.request.CreateLrRequest;
import com.feros.api.dto.request.LrChargeRequest;
import com.feros.api.dto.request.LrCheckpostRequest;
import com.feros.api.dto.request.UpdateLrRequest;
import com.feros.api.dto.response.LrChargeResponse;
import com.feros.api.dto.response.LrCheckpostResponse;
import com.feros.api.dto.response.LrResponse;

import java.util.List;

public interface LrService {
    LrResponse createLr(CreateLrRequest request);
    LrResponse getLrById(Long id);
    List<LrResponse> getAllLrs();
    List<LrResponse> getLrsByOrder(Long orderId);
    LrResponse updateLr(Long id, UpdateLrRequest request);
    LrCheckpostResponse addCheckpost(Long lrId, LrCheckpostRequest request);
    List<LrCheckpostResponse> getCheckposts(Long lrId);
    LrChargeResponse addCharge(Long lrId, LrChargeRequest request);
    List<LrChargeResponse> getCharges(Long lrId);
}