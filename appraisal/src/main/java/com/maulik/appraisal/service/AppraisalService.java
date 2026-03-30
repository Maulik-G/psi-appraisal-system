package com.maulik.appraisal.service;

import com.maulik.appraisal.dto.*;
import com.maulik.appraisal.entity.*;
import com.maulik.appraisal.entity.enums.*;
import com.maulik.appraisal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppraisalService {

    private final AppraisalRepository appraisalRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public AppraisalResponse create(AppraisalRequest request) {
        User employee = userRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        User manager = userRepository.findById(request.getManagerId())
                .orElseThrow(() -> new RuntimeException("Manager not found"));

        Appraisal appraisal = Appraisal.builder()
                .cycleName(request.getCycleName())
                .startDate(LocalDate.parse(request.getStartDate()))
                .endDate(LocalDate.parse(request.getEndDate()))
                .employee(employee)
                .manager(manager)
                .status(AppraisalStatus.PENDING)
                .build();

        Appraisal saved = appraisalRepository.save(appraisal);

        notificationService.send(employee, "New appraisal created: " + request.getCycleName());
        notificationService.send(manager, "New appraisal assigned for review: " + request.getCycleName());

        return toResponse(saved);
    }

    @Transactional
    public AppraisalResponse submitSelfAssessment(Long id, SelfAssessmentRequest request, Long employeeId) {
        Appraisal appraisal = appraisalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appraisal not found"));

        if (!appraisal.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Not authorized to submit self-assessment for this appraisal");
        }
        if (appraisal.getStatus() != AppraisalStatus.PENDING) {
            throw new RuntimeException("Self-assessment can only be submitted when status is PENDING");
        }

        appraisal.setSelfAssessment(request.getSelfAssessment());
        appraisal.setStatus(AppraisalStatus.SELF_SUBMITTED);

        notificationService.send(appraisal.getManager(),
                "Employee " + appraisal.getEmployee().getFullName() + " submitted self-assessment for " + appraisal.getCycleName());

        return toResponse(appraisalRepository.save(appraisal));
    }

    @Transactional
    public AppraisalResponse managerReview(Long id, ManagerReviewRequest request, Long managerId) {
        Appraisal appraisal = appraisalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appraisal not found"));

        if (!appraisal.getManager().getId().equals(managerId)) {
            throw new RuntimeException("Not authorized to review this appraisal");
        }
        if (appraisal.getStatus() != AppraisalStatus.SELF_SUBMITTED) {
            throw new RuntimeException("Manager review can only be done when status is SELF_SUBMITTED");
        }

        appraisal.setManagerComment(request.getManagerComment());
        appraisal.setRating(request.getRating());
        appraisal.setStatus(AppraisalStatus.MANAGER_REVIEWED);

        notificationService.send(appraisal.getEmployee(),
                "Manager reviewed your appraisal: " + appraisal.getCycleName());

        return toResponse(appraisalRepository.save(appraisal));
    }

    @Transactional
    public AppraisalResponse approve(Long id) {
        Appraisal appraisal = appraisalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appraisal not found"));

        if (appraisal.getStatus() != AppraisalStatus.MANAGER_REVIEWED) {
            throw new RuntimeException("Appraisal can only be approved when status is MANAGER_REVIEWED");
        }

        appraisal.setStatus(AppraisalStatus.APPROVED);

        notificationService.send(appraisal.getEmployee(),
                "Your appraisal has been approved: " + appraisal.getCycleName());
        notificationService.send(appraisal.getManager(),
                "Appraisal approved for " + appraisal.getEmployee().getFullName());

        return toResponse(appraisalRepository.save(appraisal));
    }

    @Transactional
    public AppraisalResponse acknowledge(Long id, Long employeeId) {
        Appraisal appraisal = appraisalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appraisal not found"));

        if (!appraisal.getEmployee().getId().equals(employeeId)) {
            throw new RuntimeException("Not authorized to acknowledge this appraisal");
        }
        if (appraisal.getStatus() != AppraisalStatus.APPROVED) {
            throw new RuntimeException("Appraisal can only be acknowledged when status is APPROVED");
        }

        appraisal.setStatus(AppraisalStatus.ACKNOWLEDGED);
        return toResponse(appraisalRepository.save(appraisal));
    }

    public List<AppraisalResponse> getAll() {
        return appraisalRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public AppraisalResponse getById(Long id) {
        return toResponse(appraisalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appraisal not found")));
    }

    public List<AppraisalResponse> getByEmployee(Long employeeId) {
        return appraisalRepository.findByEmployeeId(employeeId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<AppraisalResponse> getByManager(Long managerId) {
        return appraisalRepository.findByManagerId(managerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private AppraisalResponse toResponse(Appraisal a) {
        return AppraisalResponse.builder()
                .id(a.getId())
                .cycleName(a.getCycleName())
                .startDate(a.getStartDate())
                .endDate(a.getEndDate())
                .employeeId(a.getEmployee().getId())
                .employeeName(a.getEmployee().getFullName())
                .managerId(a.getManager().getId())
                .managerName(a.getManager().getFullName())
                .status(a.getStatus().name())
                .selfAssessment(a.getSelfAssessment())
                .managerComment(a.getManagerComment())
                .rating(a.getRating())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
