package com.psi.appraisal.services.impl;

import com.psi.appraisal.dtos.AppraisalResponse;
import com.psi.appraisal.dtos.ApproveRequest;
import com.psi.appraisal.dtos.BulkCycleRequest;
import com.psi.appraisal.dtos.BulkCycleResponse;
import com.psi.appraisal.dtos.CreateAppraisalRequest;
import com.psi.appraisal.dtos.ManagerReviewRequest;
import com.psi.appraisal.dtos.SelfAssessmentRequest;
import com.psi.appraisal.entity.Appraisal;
import com.psi.appraisal.entity.Notification.Type;
import com.psi.appraisal.entity.User;
import com.psi.appraisal.entity.enums.AppraisalStatus;
import com.psi.appraisal.entity.enums.CycleStatus;
import com.psi.appraisal.entity.enums.Role;
import com.psi.appraisal.exception.InvalidStatusTransitionException;
import com.psi.appraisal.exception.UnauthorizedAccessException;
import com.psi.appraisal.repository.AppraisalRepository;
import com.psi.appraisal.repository.UserRepository;
import com.psi.appraisal.services.AppraisalService;
import com.psi.appraisal.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppraisalServiceImpl implements AppraisalService {

    private final AppraisalRepository appraisalRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    // ── Create ────────────────────────────────────────────────────

    @Override
    @Transactional
    public AppraisalResponse createAppraisal(CreateAppraisalRequest request) {
        if (appraisalRepository.existsByCycleNameAndEmployeeId(
                request.getCycleName(), request.getEmployeeId())) {
            throw new RuntimeException("Appraisal already exists for this employee in cycle: "
                    + request.getCycleName());
        }

        User employee = findUserById(request.getEmployeeId());
        User manager  = findUserById(request.getManagerId());

        Appraisal appraisal = Appraisal.builder()
                .cycleName(request.getCycleName())
                .cycleStartDate(request.getCycleStartDate())
                .cycleEndDate(request.getCycleEndDate())
                .cycleStatus(CycleStatus.ACTIVE)
                .employee(employee)
                .manager(manager)
                .appraisalStatus(AppraisalStatus.DRAFT)
                .build();

        appraisalRepository.save(appraisal);

        notificationService.send(
                employee.getId(),
                "Appraisal cycle started",
                "Your appraisal for cycle '" + request.getCycleName()
                        + "' has been created. Please submit your self-assessment.",
                Type.CYCLE_STARTED
        );

        return mapToResponse(appraisal);
    }

    @Override
    @Transactional
    public BulkCycleResponse createBulkCycle(BulkCycleRequest request) {
        // Fetch all active users who have a manager (both EMPLOYEE and MANAGER roles)
        List<User> employees = request.getDepartmentId() != null
                ? userRepository.findByDepartmentIdAndIsActiveTrue(request.getDepartmentId())
                      .stream()
                      .filter(u -> u.getRole() == Role.EMPLOYEE || u.getRole() == Role.MANAGER)
                      .collect(Collectors.toList())
                : userRepository.findByIsActiveTrue()
                      .stream()
                      .filter(u -> u.getRole() == Role.EMPLOYEE || u.getRole() == Role.MANAGER)
                      .collect(Collectors.toList());

        int created = 0, skippedAlreadyExists = 0, skippedNoManager = 0;

        for (User employee : employees) {
            if (employee.getManager() == null) {
                log.warn("Skipping employee {} (id={}) — no manager assigned",
                        employee.getFullName(), employee.getId());
                skippedNoManager++;
                continue;
            }
            if (appraisalRepository.existsByCycleNameAndEmployeeId(
                    request.getCycleName(), employee.getId())) {
                log.info("Skipping employee {} — appraisal already exists for cycle '{}'",
                        employee.getFullName(), request.getCycleName());
                skippedAlreadyExists++;
                continue;
            }

            Appraisal appraisal = Appraisal.builder()
                    .cycleName(request.getCycleName())
                    .cycleStartDate(request.getCycleStartDate())
                    .cycleEndDate(request.getCycleEndDate())
                    .cycleStatus(CycleStatus.ACTIVE)
                    .employee(employee)
                    .manager(employee.getManager())
                    .appraisalStatus(AppraisalStatus.DRAFT)
                    .build();

            appraisalRepository.save(appraisal);

            notificationService.send(
                    employee.getId(),
                    "Appraisal cycle started",
                    "Your appraisal for cycle '" + request.getCycleName()
                            + "' has been created. Please submit your self-assessment.",
                    Type.CYCLE_STARTED
            );
            created++;
        }

        log.info("Bulk cycle '{}' — created: {}, skippedAlreadyExists: {}, skippedNoManager: {}",
                request.getCycleName(), created, skippedAlreadyExists, skippedNoManager);

        return new BulkCycleResponse(request.getCycleName(), employees.size(),
                created, skippedAlreadyExists, skippedNoManager);
    }

    // ── Read ──────────────────────────────────────────────────────

    @Override
    public List<AppraisalResponse> getMyAppraisals(Long employeeId) {
        return appraisalRepository.findByEmployeeId(employeeId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<AppraisalResponse> getTeamAppraisals(Long managerId) {
        return appraisalRepository.findByManagerId(managerId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public AppraisalResponse getAppraisalById(Long appraisalId, Long requesterId) {
        Appraisal appraisal = findAppraisalById(appraisalId);
        boolean isEmployee = appraisal.getEmployee().getId().equals(requesterId);
        boolean isManager  = appraisal.getManager().getId().equals(requesterId);
        if (!isEmployee && !isManager)
            throw new UnauthorizedAccessException("Access denied: you are not part of this appraisal");
        return mapToResponse(appraisal);
    }

    // ── Self-assessment draft ─────────────────────────────────────

    @Override
    @Transactional
    public AppraisalResponse submitGoals(Long appraisalId, Long employeeId) {
        // Deprecated: Managers now set goals directly.
        throw new InvalidStatusTransitionException("Goal submission is managed by your manager.");
    }

    @Override
    @Transactional
    public AppraisalResponse approveGoals(Long appraisalId, Long managerId) {
        Appraisal appraisal = findAppraisalById(appraisalId);
        requireManager(appraisal, managerId);

        if (appraisal.getAppraisalStatus() != AppraisalStatus.DRAFT) {
            throw new InvalidStatusTransitionException("Targets already set. Current status: " + appraisal.getAppraisalStatus());
        }

        appraisal.setAppraisalStatus(AppraisalStatus.GOALS_APPROVED);
        appraisalRepository.save(appraisal);

        notificationService.send(
                appraisal.getEmployee().getId(),
                "Appraisal Targets Finalized",
                "Your manager has finalized the targets for this cycle. You can now track your progress.",
                Type.STATUS_CHANGED
        );

        return mapToResponse(appraisal);
    }

    // ── Self-assessment draft ─────────────────────────────────────

    @Override
    @Transactional
    public AppraisalResponse saveSelfAssessmentDraft(Long appraisalId,
                                                     SelfAssessmentRequest request,
                                                     Long employeeId) {
        Appraisal appraisal = findAppraisalById(appraisalId);
        requireEmployee(appraisal, employeeId);

        AppraisalStatus status = appraisal.getAppraisalStatus();
        if (status != AppraisalStatus.GOALS_APPROVED && status != AppraisalStatus.SELF_SUBMITTED) {
             // We allow saving draft even if already self-submitted? 
             // Actually, "Employees cannot submit after deadline" and "strict workflow".
             // Let's stick to: can only edit draft if status is GOALS_APPROVED.
        }
        
        if (status != AppraisalStatus.GOALS_APPROVED) {
            throw new InvalidStatusTransitionException(
                    "Cannot save self-assessment. Required status: GOALS_APPROVED. Current: " + status);
        }

        applySelfAssessmentFields(appraisal, request);
        appraisalRepository.save(appraisal);

        return mapToResponse(appraisal);
    }

    // ── Self-assessment submit ────────────────────────────────────

    @Override
    @Transactional
    public AppraisalResponse submitSelfAssessment(Long appraisalId,
                                                  SelfAssessmentRequest request,
                                                  Long employeeId) {
        Appraisal appraisal = findAppraisalById(appraisalId);
        requireEmployee(appraisal, employeeId);

        AppraisalStatus status = appraisal.getAppraisalStatus();
        if (status != AppraisalStatus.GOALS_APPROVED) {
            throw new InvalidStatusTransitionException(
                    "Cannot submit self-assessment. Required status: GOALS_APPROVED. Current: " + status);
        }

        applySelfAssessmentFields(appraisal, request);
        appraisal.setAppraisalStatus(AppraisalStatus.SELF_SUBMITTED);
        appraisal.setSubmittedAt(LocalDateTime.now());
        appraisalRepository.save(appraisal);

        notificationService.send(
                appraisal.getManager().getId(),
                "Self-assessment submitted",
                appraisal.getEmployee().getFullName() + " has submitted their self-assessment for '"
                        + appraisal.getCycleName() + "'. Please review and rate.",
                Type.SELF_ASSESSMENT_SUBMITTED
        );

        return mapToResponse(appraisal);
    }

    // ── Manager review draft ──────────────────────────────────────

    @Override
    @Transactional
    public AppraisalResponse saveManagerReviewDraft(Long appraisalId,
                                                    ManagerReviewRequest request,
                                                    Long managerId) {
        Appraisal appraisal = findAppraisalById(appraisalId);
        requireManager(appraisal, managerId);

        AppraisalStatus status = appraisal.getAppraisalStatus();
        if (status != AppraisalStatus.SELF_SUBMITTED && status != AppraisalStatus.MANAGER_REVIEWED) {
            throw new InvalidStatusTransitionException(
                    "Cannot save manager draft. Current status: " + status);
        }

        applyManagerReviewFields(appraisal, request);
        appraisalRepository.save(appraisal);

        return mapToResponse(appraisal);
    }

    // ── Manager review submit ─────────────────────────────────────

    @Override
    @Transactional
    public AppraisalResponse submitManagerReview(Long appraisalId,
                                                 ManagerReviewRequest request,
                                                 Long managerId) {
        Appraisal appraisal = findAppraisalById(appraisalId);
        requireManager(appraisal, managerId);

        AppraisalStatus status = appraisal.getAppraisalStatus();
        if (status != AppraisalStatus.SELF_SUBMITTED) {
            throw new InvalidStatusTransitionException(
                    "Cannot submit manager review. Current status: " + status);
        }

        applyManagerReviewFields(appraisal, request);
        appraisal.setAppraisalStatus(AppraisalStatus.MANAGER_REVIEWED);
        appraisalRepository.save(appraisal);

        // Notify all active HR users
        List<User> hrUsers = userRepository.findByRoleAndIsActiveTrue(Role.HR);
        for (User hr : hrUsers) {
            notificationService.send(
                    hr.getId(),
                    "Appraisal ready for approval",
                    appraisal.getEmployee().getFullName() + "'s appraisal for '"
                            + appraisal.getCycleName() + "' is ready for your approval.",
                    Type.MANAGER_REVIEW_DONE
            );
        }

        // Notify the employee
        notificationService.send(
                appraisal.getEmployee().getId(),
                "Your appraisal has been reviewed",
                "Your manager has completed their review for '"
                        + appraisal.getCycleName() + "'. Awaiting HR approval.",
                Type.MANAGER_REVIEW_DONE
        );

        return mapToResponse(appraisal);
    }

    // ── Approve ───────────────────────────────────────────────────

    @Override
    @Transactional
    public AppraisalResponse approveAppraisal(Long appraisalId, ApproveRequest request) {
        Appraisal appraisal = findAppraisalById(appraisalId);

        if (appraisal.getAppraisalStatus() != AppraisalStatus.MANAGER_REVIEWED) {
            throw new InvalidStatusTransitionException(
                    "Cannot approve. Current status: " + appraisal.getAppraisalStatus());
        }

        appraisal.setAppraisalStatus(AppraisalStatus.FINALIZED);
        appraisal.setApprovedAt(LocalDateTime.now());
        appraisal.setHrComments(request.getHrComments());
        
        // Final score override if provided
        if (request.getFinalRating() != null) {
            appraisal.setManagerRating(request.getFinalRating()); // Or add a dedicated finalRating field
        }

        appraisalRepository.save(appraisal);

        notificationService.send(
                appraisal.getEmployee().getId(),
                "Appraisal finalized",
                "Your appraisal for '" + appraisal.getCycleName()
                        + "' has been finalized and published.",
                Type.STATUS_CHANGED
        );

        return mapToResponse(appraisal);
    }

    // ── Acknowledge ───────────────────────────────────────────────

    @Override
    @Transactional
    public AppraisalResponse acknowledgeAppraisal(Long appraisalId, Long employeeId) {
        Appraisal appraisal = findAppraisalById(appraisalId);
        requireEmployee(appraisal, employeeId);

        if (appraisal.getAppraisalStatus() != AppraisalStatus.FINALIZED) {
            throw new InvalidStatusTransitionException(
                    "Cannot acknowledge. Status is not FINALIZED.");
        }

        // Just mark as acknowledged (already FINALIZED)
        appraisalRepository.save(appraisal);

        return mapToResponse(appraisal);
    }

    // ── Private helpers ───────────────────────────────────────────

    private void requireEmployee(Appraisal appraisal, Long employeeId) {
        if (!appraisal.getEmployee().getId().equals(employeeId))
            throw new UnauthorizedAccessException("Access denied: this is not your appraisal");
    }

    private void requireManager(Appraisal appraisal, Long managerId) {
        if (!appraisal.getManager().getId().equals(managerId))
            throw new UnauthorizedAccessException("Access denied: you are not the manager for this appraisal");
    }

    private void applySelfAssessmentFields(Appraisal appraisal, SelfAssessmentRequest request) {
        appraisal.setWhatWentWell(request.getWhatWentWell());
        appraisal.setWhatToImprove(request.getWhatToImprove());
        appraisal.setAchievements(request.getAchievements());
        appraisal.setSelfRating(request.getSelfRating());
    }

    private void applyManagerReviewFields(Appraisal appraisal, ManagerReviewRequest request) {
        appraisal.setManagerStrengths(request.getManagerStrengths());
        appraisal.setManagerImprovements(request.getManagerImprovements());
        appraisal.setManagerComments(request.getManagerComments());
        appraisal.setManagerRating(request.getManagerRating());
    }

    private Appraisal findAppraisalById(Long id) {
        return appraisalRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Appraisal not found with id: " + id));
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    private AppraisalResponse mapToResponse(Appraisal appraisal) {
        AppraisalResponse response = new AppraisalResponse();
        response.setId(appraisal.getId());
        response.setCycleName(appraisal.getCycleName());
        response.setCycleStartDate(appraisal.getCycleStartDate());
        response.setCycleEndDate(appraisal.getCycleEndDate());
        response.setCycleStatus(appraisal.getCycleStatus());
        response.setEmployeeId(appraisal.getEmployee().getId());
        response.setEmployeeName(appraisal.getEmployee().getFullName());
        response.setEmployeeJobTitle(appraisal.getEmployee().getJobTitle());
        if (appraisal.getEmployee().getDepartment() != null)
            response.setEmployeeDepartment(appraisal.getEmployee().getDepartment().getName());
        response.setManagerId(appraisal.getManager().getId());
        response.setManagerName(appraisal.getManager().getFullName());
        response.setWhatWentWell(appraisal.getWhatWentWell());
        response.setWhatToImprove(appraisal.getWhatToImprove());
        response.setAchievements(appraisal.getAchievements());
        response.setSelfRating(appraisal.getSelfRating());
        response.setManagerStrengths(appraisal.getManagerStrengths());
        response.setManagerImprovements(appraisal.getManagerImprovements());
        response.setManagerComments(appraisal.getManagerComments());
        response.setManagerRating(appraisal.getManagerRating());
        response.setAppraisalStatus(appraisal.getAppraisalStatus());
        response.setSubmittedAt(appraisal.getSubmittedAt());
        response.setApprovedAt(appraisal.getApprovedAt());
        response.setHrComments(appraisal.getHrComments());
        response.setCreatedAt(appraisal.getCreatedAt());
        return response;
    }
}
