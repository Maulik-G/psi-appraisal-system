package com.psi.appraisal.dtos;

import com.psi.appraisal.entity.Goal.Status;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GoalResponse {

    private Long id;
    private Long appraisalId;
    private Long employeeId;
    private String employeeName;
    private String title;
    private String description;
    private Status status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}
