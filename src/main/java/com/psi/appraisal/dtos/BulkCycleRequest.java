package com.psi.appraisal.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

@Getter
@Setter
public class BulkCycleRequest {

    @NotBlank(message = "Cycle name is required")
    private String cycleName;

    @NotNull(message = "Cycle start date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate cycleStartDate;

    @NotNull(message = "Cycle end date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate cycleEndDate;

    // Optional — if set, only create for employees in this department
    private Long departmentId;
}
