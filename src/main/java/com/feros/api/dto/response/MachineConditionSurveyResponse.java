package com.feros.api.dto.response;

import com.feros.api.enums.SurveyType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MachineConditionSurveyResponse {
    private Long id;
    private Long machineAssignmentId;
    private SurveyType surveyType;
    private LocalDate surveyDate;
    private BigDecimal hmrAtSurvey;
    private String conditionNotes;
    private List<String> photos;
    private String surveyedBy;
    private LocalDateTime createdAt;
}
