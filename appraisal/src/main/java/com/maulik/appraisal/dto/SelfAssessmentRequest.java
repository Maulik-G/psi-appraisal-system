package com.maulik.appraisal.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SelfAssessmentRequest {
    @NotBlank(message = "Self assessment is required")
    private String selfAssessment;
}
