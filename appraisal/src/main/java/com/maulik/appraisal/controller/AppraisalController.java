package com.maulik.appraisal.controller;

import com.maulik.appraisal.dto.*;
import com.maulik.appraisal.entity.User;
import com.maulik.appraisal.service.AppraisalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appraisals")
@RequiredArgsConstructor
public class AppraisalController {

    private final AppraisalService appraisalService;

    @PostMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<AppraisalResponse>> create(@Valid @RequestBody AppraisalRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Appraisal created", appraisalService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AppraisalResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(appraisalService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AppraisalResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(appraisalService.getById(id)));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<AppraisalResponse>>> getMyAppraisals() {
        User user = com.maulik.appraisal.security.AuthUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(appraisalService.getByEmployee(user.getId())));
    }

    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    public ResponseEntity<ApiResponse<List<AppraisalResponse>>> getTeamAppraisals() {
        User user = com.maulik.appraisal.security.AuthUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(appraisalService.getByManager(user.getId())));
    }

    @PutMapping("/{id}/self-assess")
    public ResponseEntity<ApiResponse<AppraisalResponse>> submitSelfAssessment(
            @PathVariable Long id,
            @Valid @RequestBody SelfAssessmentRequest request) {
        User user = com.maulik.appraisal.security.AuthUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Self-assessment submitted",
                appraisalService.submitSelfAssessment(id, request, user.getId())));
    }

    @PutMapping("/{id}/manager-review")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    public ResponseEntity<ApiResponse<AppraisalResponse>> managerReview(
            @PathVariable Long id,
            @Valid @RequestBody ManagerReviewRequest request) {
        User user = com.maulik.appraisal.security.AuthUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Manager review submitted",
                appraisalService.managerReview(id, request, user.getId())));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<AppraisalResponse>> approve(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Appraisal approved", appraisalService.approve(id)));
    }

    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<AppraisalResponse>> acknowledge(
            @PathVariable Long id) {
        User user = com.maulik.appraisal.security.AuthUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Appraisal acknowledged",
                appraisalService.acknowledge(id, user.getId())));
    }
}
