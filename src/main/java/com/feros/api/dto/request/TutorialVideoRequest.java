package com.feros.api.dto.request;

import lombok.Data;

@Data
public class TutorialVideoRequest {
    private String role;
    private String language;
    private String featureTitle;
    private String youtubeUrl;
    private Integer sortOrder;
    private Boolean isActive;
}
