package com.maulik.appraisal.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GoalProgressRequest {
    @NotNull(message = "Progress is required")
    @Min(0) @Max(100)
    private Integer progress;

    private String status;
}
