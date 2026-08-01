package com.feros.api.service;

import com.feros.api.dto.request.TutorialVideoRequest;
import com.feros.api.dto.response.TutorialVideoResponse;

import java.util.List;

public interface TutorialVideoService {
    List<TutorialVideoResponse> getForMobile(String language);
    List<TutorialVideoResponse> getAll();
    TutorialVideoResponse create(TutorialVideoRequest request);
    TutorialVideoResponse update(Long id, TutorialVideoRequest request);
    void delete(Long id);
}
