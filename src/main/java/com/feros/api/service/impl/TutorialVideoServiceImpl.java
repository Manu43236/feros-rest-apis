package com.feros.api.service.impl;

import com.feros.api.dto.request.TutorialVideoRequest;
import com.feros.api.dto.response.TutorialVideoResponse;
import com.feros.api.entity.TutorialVideo;
import com.feros.api.exception.FerosException;
import com.feros.api.repository.TutorialVideoRepository;
import com.feros.api.service.TutorialVideoService;
import com.feros.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TutorialVideoServiceImpl implements TutorialVideoService {

    private final TutorialVideoRepository repository;

    @Override
    public List<TutorialVideoResponse> getForMobile(String language) {
        String role = SecurityUtil.getCurrentRole();
        return repository.findForRole(role, language)
                .stream().map(TutorialVideoResponse::from).toList();
    }

    @Override
    public List<TutorialVideoResponse> getAll() {
        return repository.findAllByOrderBySortOrderAsc()
                .stream().map(TutorialVideoResponse::from).toList();
    }

    @Override
    public TutorialVideoResponse create(TutorialVideoRequest req) {
        TutorialVideo v = TutorialVideo.builder()
                .role(req.getRole())
                .language(req.getLanguage())
                .featureTitle(req.getFeatureTitle())
                .youtubeUrl(req.getYoutubeUrl())
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .build();
        return TutorialVideoResponse.from(repository.save(v));
    }

    @Override
    public TutorialVideoResponse update(Long id, TutorialVideoRequest req) {
        TutorialVideo v = repository.findById(id)
                .orElseThrow(() -> new FerosException("Tutorial video not found", HttpStatus.NOT_FOUND));
        v.setRole(req.getRole());
        v.setLanguage(req.getLanguage());
        v.setFeatureTitle(req.getFeatureTitle());
        v.setYoutubeUrl(req.getYoutubeUrl());
        if (req.getSortOrder() != null) v.setSortOrder(req.getSortOrder());
        if (req.getIsActive() != null) v.setIsActive(req.getIsActive());
        return TutorialVideoResponse.from(repository.save(v));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id))
            throw new FerosException("Tutorial video not found", HttpStatus.NOT_FOUND);
        repository.deleteById(id);
    }
}
