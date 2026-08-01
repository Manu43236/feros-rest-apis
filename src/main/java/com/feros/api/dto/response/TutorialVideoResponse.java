package com.feros.api.dto.response;

import com.feros.api.entity.TutorialVideo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TutorialVideoResponse {
    private Long id;
    private String role;
    private String language;
    private String featureTitle;
    private String youtubeUrl;
    private Integer sortOrder;
    private Boolean isActive;

    public static TutorialVideoResponse from(TutorialVideo v) {
        return TutorialVideoResponse.builder()
                .id(v.getId())
                .role(v.getRole())
                .language(v.getLanguage())
                .featureTitle(v.getFeatureTitle())
                .youtubeUrl(v.getYoutubeUrl())
                .sortOrder(v.getSortOrder())
                .isActive(v.getIsActive())
                .build();
    }
}
