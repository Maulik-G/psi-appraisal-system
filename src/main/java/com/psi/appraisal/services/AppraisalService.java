package com.psi.appraisal.services;

import java.util.List;

import com.psi.appraisal.dtos.AppraisalResponse;
import com.psi.appraisal.dtos.ApproveRequest;
import com.psi.appraisal.dtos.BulkCycleRequest;
import com.psi.appraisal.dtos.BulkCycleResponse;
import com.psi.appraisal.dtos.CreateAppraisalRequest;
import com.psi.appraisal.dtos.ManagerReviewRequest;
import com.psi.appraisal.dtos.SelfAssessmentRequest;

public interface AppraisalService {

    // HR: create a new appraisal for an employee in a cycle
    AppraisalResponse createAppraisal(CreateAppraisalRequest request);

    // HR: bulk create one appraisal per active employee for a cycle
    BulkCycleResponse createBulkCycle(BulkCycleRequest request);

    // Employee: view all their own appraisals
    List<AppraisalResponse> getMyAppraisals(Long employeeId);

    // Manager: view all appraisals for their team
    List<AppraisalResponse> getTeamAppraisals(Long managerId);

    // Any role: view one appraisal by ID (with ownership check)
    AppraisalResponse getAppraisalById(Long appraisalId, Long requesterId);

    // Employee: submit goals for approval
    AppraisalResponse submitGoals(Long appraisalId, Long employeeId);

    // Manager: approve goals — moves status to GOALS_APPROVED
    AppraisalResponse approveGoals(Long appraisalId, Long managerId);

    // Employee: save draft of self-assessment (status remains unchanged)
    AppraisalResponse saveSelfAssessmentDraft(Long appraisalId, SelfAssessmentRequest request, Long employeeId);

    // Employee: final submit self-assessment — status moves to SELF_SUBMITTED
    AppraisalResponse submitSelfAssessment(Long appraisalId, SelfAssessmentRequest request, Long employeeId);

    // Manager: save draft of review (status remains unchanged)
    AppraisalResponse saveManagerReviewDraft(Long appraisalId, ManagerReviewRequest request, Long managerId);

    // Manager: final submit review — status moves to MANAGER_REVIEWED
    AppraisalResponse submitManagerReview(Long appraisalId, ManagerReviewRequest request, Long managerId);

    // HR: finalize result — moves status to FINALIZED
    AppraisalResponse approveAppraisal(Long appraisalId, ApproveRequest request);

    // Employee: acknowledge result (status stays FINALIZED or moves to ARCHIVED if applicable)
    AppraisalResponse acknowledgeAppraisal(Long appraisalId, Long employeeId);
}
