package com.feros.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tutorial_videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorialVideo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(nullable = false, length = 10)
    private String language;

    @Column(name = "feature_title", nullable = false, length = 150)
    private String featureTitle;

    @Column(name = "youtube_url", nullable = false, length = 500)
    private String youtubeUrl;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
