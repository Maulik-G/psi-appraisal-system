package com.maulik.appraisal.repository;

import com.maulik.appraisal.entity.Appraisal;
import com.maulik.appraisal.entity.enums.AppraisalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppraisalRepository extends JpaRepository<Appraisal, Long> {
    List<Appraisal> findByEmployeeId(Long employeeId);
    List<Appraisal> findByManagerId(Long managerId);
    List<Appraisal> findByStatus(AppraisalStatus status);
    List<Appraisal> findByEmployeeIdAndStatus(Long employeeId, AppraisalStatus status);
}
