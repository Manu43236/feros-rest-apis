package com.feros.api.entity;

import com.feros.api.enums.SurveyType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "machine_condition_surveys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineConditionSurvey extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_assignment_id", nullable = false)
    private MachineAssignment machineAssignment;

    @Enumerated(EnumType.STRING)
    @Column(name = "survey_type", nullable = false)
    private SurveyType surveyType;

    @Column(name = "survey_date", nullable = false)
    private LocalDate surveyDate;

    @Column(name = "hmr_at_survey", precision = 10, scale = 1)
    private BigDecimal hmrAtSurvey;

    @Column(name = "condition_notes", columnDefinition = "TEXT")
    private String conditionNotes;

    @Column(name = "photos", columnDefinition = "TEXT")
    private String photos;

    @Column(name = "surveyed_by")
    private String surveyedBy;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
