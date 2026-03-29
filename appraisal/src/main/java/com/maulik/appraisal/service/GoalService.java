package com.maulik.appraisal.service;

import com.maulik.appraisal.dto.*;
import com.maulik.appraisal.entity.*;
import com.maulik.appraisal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;

    public GoalResponse create(GoalRequest request, Long managerId) {
        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        Goal goal = Goal.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(LocalDate.parse(request.getDueDate()))
                .employee(employee)
                .manager(manager)
                .progress(0)
                .status(GoalStatus.NOT_STARTED)
                .build();

        return toResponse(goalRepository.save(goal));
    }

    @Transactional
    public GoalResponse updateProgress(Long goalId, GoalProgressRequest request, Long employeeId) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (!goal.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Not authorized to update this goal");
        }

        goal.setProgress(request.getProgress());
        if (request.getStatus() != null) {
            goal.setStatus(GoalStatus.valueOf(request.getStatus().toUpperCase()));
        } else if (request.getProgress() >= 100) {
            goal.setStatus(GoalStatus.COMPLETED);
        } else if (request.getProgress() > 0) {
            goal.setStatus(GoalStatus.IN_PROGRESS);
        }

        return toResponse(goalRepository.save(goal));
    }

    public List<GoalResponse> getByEmployee(Long employeeId) {
        return goalRepository.findByEmployeeId(employeeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<GoalResponse> getByManager(Long managerId) {
        return goalRepository.findByManagerId(managerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<GoalResponse> getAll() {
        return goalRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    private GoalResponse toResponse(Goal g) {
        return GoalResponse.builder()
                .id(g.getId())
                .title(g.getTitle())
                .description(g.getDescription())
                .progress(g.getProgress())
                .status(g.getStatus().name())
                .dueDate(g.getDueDate())
                .employeeId(g.getEmployee().getId())
                .employeeName(g.getEmployee().getFullName())
                .managerId(g.getManager().getId())
                .managerName(g.getManager().getFullName())
                .createdAt(g.getCreatedAt())
                .build();
    }
}
