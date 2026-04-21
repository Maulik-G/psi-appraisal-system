package com.psi.appraisal.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveRequest {
    private String hrComments;
    private Integer finalRating;
}
