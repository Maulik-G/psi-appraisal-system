package com.maulik.appraisal.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class GoalRequest {
    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Due date is required")
    private String dueDate;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;
}
