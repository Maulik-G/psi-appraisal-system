package com.maulik.appraisal.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GoalResponse {
    private Long id;
    private String title;
    private String description;
    private Integer progress;
    private String status;
    private LocalDate dueDate;
    private Long employeeId;
    private String employeeName;
    private Long managerId;
    private String managerName;
    private LocalDateTime createdAt;
}
