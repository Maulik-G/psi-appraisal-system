package com.maulik.appraisal.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ManagerReviewRequest {
    @NotBlank(message = "Manager comment is required")
    private String managerComment;

    @NotNull(message = "Rating is required")
    @Min(1) @Max(5)
    private Integer rating;
}
