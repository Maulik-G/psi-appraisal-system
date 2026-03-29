package com.maulik.appraisal.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private String departmentName;
    private Long departmentId;
    private String managerName;
    private Long managerId;
    private LocalDateTime createdAt;
}
