package com.feros.api.dto.request;

import com.feros.api.enums.SurveyType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MachineConditionSurveyRequest {

    @NotNull(message = "Survey type is required")
    private SurveyType surveyType;

    @NotNull(message = "Survey date is required")
    private LocalDate surveyDate;

    private BigDecimal hmrAtSurvey;
    private String conditionNotes;
    private List<String> photos;
    private String surveyedBy;
}
