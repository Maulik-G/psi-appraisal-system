package com.maulik.appraisal.controller;

import com.maulik.appraisal.dto.*;
import com.maulik.appraisal.entity.User;
import com.maulik.appraisal.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    public ResponseEntity<ApiResponse<GoalResponse>> create(
            @Valid @RequestBody GoalRequest request) {
        User user = com.maulik.appraisal.security.AuthUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Goal created", goalService.create(request, user.getId())));
    }

    @PutMapping("/{id}/progress")
    public ResponseEntity<ApiResponse<GoalResponse>> updateProgress(
            @PathVariable Long id,
            @Valid @RequestBody GoalProgressRequest request) {
        User user = com.maulik.appraisal.security.AuthUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Progress updated",
                goalService.updateProgress(id, request, user.getId())));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getMyGoals() {
        User user = com.maulik.appraisal.security.AuthUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(goalService.getByEmployee(user.getId())));
    }

    @GetMapping("/team")
    @PreAuthorize("hasAnyRole('MANAGER', 'HR')")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getTeamGoals() {
        User user = com.maulik.appraisal.security.AuthUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(goalService.getByManager(user.getId())));
    }

    @GetMapping
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(goalService.getAll()));
    }
}
