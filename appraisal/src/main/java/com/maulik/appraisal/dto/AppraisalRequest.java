package com.maulik.appraisal.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AppraisalRequest {
    @NotBlank(message = "Cycle name is required")
    private String cycleName;

    @NotNull(message = "Start date is required")
    private String startDate;

    @NotNull(message = "End date is required")
    private String endDate;

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Manager ID is required")
    private Long managerId;
}
