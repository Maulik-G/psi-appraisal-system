package com.psi.appraisal.services.impl;

import com.psi.appraisal.dtos.CreateGoalRequest;
import com.psi.appraisal.dtos.GoalProgressRequest;
import com.psi.appraisal.dtos.GoalResponse;
import com.psi.appraisal.dtos.UpdateGoalRequest;
import com.psi.appraisal.entity.Appraisal;
import com.psi.appraisal.entity.Goal;
import com.psi.appraisal.entity.enums.AppraisalStatus; // Added missing import
import com.psi.appraisal.exception.InvalidStatusTransitionException; // Added missing import
import com.psi.appraisal.exception.ResourceNotFoundException;
import com.psi.appraisal.exception.UnauthorizedAccessException;
import com.psi.appraisal.repository.AppraisalRepository;
import com.psi.appraisal.repository.GoalRepository;
import com.psi.appraisal.services.GoalService;
import com.psi.appraisal.services.NotificationService;
import com.psi.appraisal.entity.Notification.Type;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final AppraisalRepository appraisalRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public GoalResponse createGoal(CreateGoalRequest request, Long managerId) {
        log.info("Creating goal: title={}, appraisalId={}, managerId={}",
                request.getTitle(), request.getAppraisalId(), managerId);

        Appraisal appraisal = appraisalRepository.findByIdWithDetails(request.getAppraisalId())
                .orElseThrow(() -> new ResourceNotFoundException("Appraisal", request.getAppraisalId()));

        if (appraisal.getAppraisalStatus() != AppraisalStatus.DRAFT) {
            throw new InvalidStatusTransitionException(
                    "Cannot add goals after they have been approved or submitted for review. Current status: " + appraisal.getAppraisalStatus());
        }

        // Logic fix: Verify manager access
        if (!appraisal.getManager().getId().equals(managerId)) {
            throw new UnauthorizedAccessException("You are not authorized to create goals for this appraisal.");
        }

        Goal goal = Goal.builder()
                .appraisal(appraisal)
                .employee(appraisal.getEmployee())
                .manager(appraisal.getManager())
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .progressPercent(0)
                .build();

        try {
            goalRepository.save(goal);
            log.info("Goal saved successfully with ID: {}", goal.getId());
            
            // Notify Employee
            notificationService.send(
                appraisal.getEmployee().getId(),
                "New Target Assigned",
                "Your manager has assigned a new target: " + goal.getTitle() + " for " + appraisal.getCycleName(),
                Type.GENERAL
            );
        } catch (Exception e) {
            log.error("Failed to save goal to database: {}", e.getMessage(), e);
            throw new RuntimeException("Database error: Could not save goal.");
        }
        return mapToResponse(goal);
    }

    @Override
    public GoalResponse getGoalById(Long goalId) {
        return mapToResponse(findById(goalId));
    }

    @Override
    public List<GoalResponse> getGoalsByAppraisal(Long appraisalId) {
        return goalRepository.findByAppraisalId(appraisalId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<GoalResponse> getGoalsByEmployee(Long employeeId) {
        return goalRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(Long goalId, UpdateGoalRequest request, Long managerId) {
        log.info("Updating goal ID: {}, managerId={}", goalId, managerId);
        Goal goal = findById(goalId);

        if (goal.getAppraisal().getAppraisalStatus() != AppraisalStatus.DRAFT) {
            throw new InvalidStatusTransitionException(
                    "Cannot update goals in status: " + goal.getAppraisal().getAppraisalStatus());
        }

        // Logic fix: Verify manager access
        if (!goal.getAppraisal().getManager().getId().equals(managerId)) {
            throw new UnauthorizedAccessException("You are not authorized to update this goal.");
        }

        if (request.getTitle() != null) goal.setTitle(request.getTitle());
        if (request.getDescription() != null) goal.setDescription(request.getDescription());
        if (request.getDueDate() != null) goal.setDueDate(request.getDueDate());

        try {
            goalRepository.save(goal);
            log.info("Goal updated successfully");

            // Notify Employee
            notificationService.send(
                goal.getEmployee().getId(),
                "Target Updated",
                "Your manager has updated the target: " + goal.getTitle(),
                Type.GENERAL
            );
        } catch (Exception e) {
            log.error("Failed to update goal: {}", e.getMessage(), e);
            throw new RuntimeException("Database error: Could not update goal.");
        }
        return mapToResponse(goal);
    }

    @Override
    @Transactional
    public GoalResponse updateProgress(Long goalId, GoalProgressRequest request, Long employeeId) {
        Goal goal = findById(goalId);

        if (!goal.getEmployee().getId().equals(employeeId)) {
            throw new UnauthorizedAccessException("Access denied: this is not your goal");
        }

        goal.setStatus(request.getStatus());
        goalRepository.save(goal);
        return mapToResponse(goal);
    }

    @Override
    @Transactional
    public void deleteGoal(Long goalId, Long managerId) {
        Goal goal = findById(goalId);

        if (goal.getAppraisal().getAppraisalStatus() != AppraisalStatus.DRAFT) {
            throw new InvalidStatusTransitionException(
                    "Cannot delete goals in status: " + goal.getAppraisal().getAppraisalStatus());
        }

        // Logic fix: Verify manager access
        if (!goal.getAppraisal().getManager().getId().equals(managerId)) {
            throw new UnauthorizedAccessException("You are not authorized to delete this goal.");
        }

        goalRepository.delete(goal);

        // Notify Employee
        notificationService.send(
            goal.getEmployee().getId(),
            "Target Removed",
            "Your manager has removed the target: " + goal.getTitle(),
            Type.GENERAL
        );
    }

    private Goal findById(Long id) {
        return goalRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", id));
    }

    private GoalResponse mapToResponse(Goal goal) {
        GoalResponse response = new GoalResponse();
        response.setId(goal.getId());
        response.setAppraisalId(goal.getAppraisal().getId());
        response.setEmployeeId(goal.getEmployee().getId());
        response.setEmployeeName(goal.getEmployee().getFullName());
        response.setTitle(goal.getTitle());
        response.setDescription(goal.getDescription());
        response.setStatus(goal.getStatus());
        response.setDueDate(goal.getDueDate());
        return response;
    }
}