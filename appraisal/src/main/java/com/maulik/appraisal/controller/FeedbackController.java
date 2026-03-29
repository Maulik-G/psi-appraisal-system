package com.maulik.appraisal.controller;

import com.maulik.appraisal.dto.*;
import com.maulik.appraisal.entity.User;
import com.maulik.appraisal.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackResponse>> create(
            @Valid @RequestBody FeedbackRequest request) {
        User user = com.maulik.appraisal.security.AuthUtils.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success("Feedback submitted",
                feedbackService.create(request, user.getId())));
    }

    @GetMapping("/appraisal/{appraisalId}")
    public ResponseEntity<ApiResponse<List<FeedbackResponse>>> getByAppraisal(@PathVariable Long appraisalId) {
        return ResponseEntity.ok(ApiResponse.success(feedbackService.getByAppraisal(appraisalId)));
    }
}
