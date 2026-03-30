package com.maulik.appraisal.service;

import com.maulik.appraisal.dto.*;
import com.maulik.appraisal.entity.*;
import com.maulik.appraisal.entity.enums.*;
import com.maulik.appraisal.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final AppraisalRepository appraisalRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public FeedbackResponse create(FeedbackRequest request, Long reviewerId) {
        FeedbackType type = FeedbackType.valueOf(request.getType().toUpperCase());

        if (feedbackRepository.existsByAppraisalIdAndReviewerIdAndType(
                request.getAppraisalId(), reviewerId, type)) {
            throw new RuntimeException("Duplicate feedback: you have already submitted " + type + " feedback for this appraisal");
        }

        Appraisal appraisal = appraisalRepository.findById(request.getAppraisalId())
                .orElseThrow(() -> new RuntimeException("Appraisal not found"));
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        Feedback feedback = Feedback.builder()
                .appraisal(appraisal)
                .reviewer(reviewer)
                .type(type)
                .comment(request.getComment())
                .rating(request.getRating())
                .build();

        Feedback saved = feedbackRepository.save(feedback);

        notificationService.send(appraisal.getEmployee(),
                reviewer.getFullName() + " submitted " + type + " feedback for " + appraisal.getCycleName());

        return toResponse(saved);
    }

    public List<FeedbackResponse> getByAppraisal(Long appraisalId) {
        return feedbackRepository.findByAppraisalId(appraisalId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private FeedbackResponse toResponse(Feedback f) {
        return FeedbackResponse.builder()
                .id(f.getId())
                .appraisalId(f.getAppraisal().getId())
                .reviewerId(f.getReviewer().getId())
                .reviewerName(f.getReviewer().getFullName())
                .type(f.getType().name())
                .comment(f.getComment())
                .rating(f.getRating())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
