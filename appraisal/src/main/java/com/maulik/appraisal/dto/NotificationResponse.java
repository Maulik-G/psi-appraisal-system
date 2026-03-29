package com.maulik.appraisal.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class NotificationResponse {
    private Long id;
    private String message;
    private Boolean read;
    private LocalDateTime createdAt;
}
