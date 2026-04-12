package com.psi.appraisal.entity.enums;

public enum AppraisalStatus {
    DRAFT,              // Planning goals
    GOALS_APPROVED,     // Manager approved goals, ready for self-assessment
    SELF_SUBMITTED,     // Employee submitted self-assessment
    MANAGER_REVIEWED,   // Manager submitted their review
    FINALIZED           // Approved by HR and acknowledged
}
