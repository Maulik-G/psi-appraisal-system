package com.maulik.appraisal.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackResponse {
    private Long id;
    private Long appraisalId;
    private Long reviewerId;
    private String reviewerName;
    private String type;
    private String comment;
    private Integer rating;
    private LocalDateTime createdAt;
}
