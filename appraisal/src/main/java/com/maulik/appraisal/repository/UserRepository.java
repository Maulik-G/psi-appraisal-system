package com.maulik.appraisal.repository;

import com.maulik.appraisal.entity.User;
import com.maulik.appraisal.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByManagerId(Long managerId);
    List<User> findByDepartmentId(Long departmentId);
}