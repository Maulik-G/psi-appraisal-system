package com.psi.appraisal.dtos;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CycleSummaryResponse {
    private String cycleName;
    private long totalAppraisals;
    private long draft;
    private long goalsApproved;
    private long selfSubmitted;
    private long managerReviewed;
    private long finalized;
    private double completionPercentage;
    private Double averageManagerRating;
}
