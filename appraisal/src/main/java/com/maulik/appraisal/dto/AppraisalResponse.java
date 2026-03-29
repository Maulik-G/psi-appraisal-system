package com.maulik.appraisal.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppraisalResponse {
    private Long id;
    private String cycleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long employeeId;
    private String employeeName;
    private Long managerId;
    private String managerName;
    private String status;
    private String selfAssessment;
    private String managerComment;
    private Integer rating;
    private LocalDateTime createdAt;
}
