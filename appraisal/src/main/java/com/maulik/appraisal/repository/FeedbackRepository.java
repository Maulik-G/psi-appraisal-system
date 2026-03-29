package com.maulik.appraisal.repository;

import com.maulik.appraisal.entity.Feedback;
import com.maulik.appraisal.entity.FeedbackType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByAppraisalId(Long appraisalId);
    boolean existsByAppraisalIdAndReviewerIdAndType(Long appraisalId, Long reviewerId, FeedbackType type);
}
