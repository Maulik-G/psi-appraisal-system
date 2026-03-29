package com.maulik.appraisal.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FeedbackRequest {
    @NotNull(message = "Appraisal ID is required")
    private Long appraisalId;

    @NotBlank(message = "Feedback type is required")
    private String type;

    @NotBlank(message = "Comment is required")
    private String comment;

    @Min(1) @Max(5)
    private Integer rating;
}
