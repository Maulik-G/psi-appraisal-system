package com.maulik.appraisal.config;

import com.maulik.appraisal.entity.Department;
import com.maulik.appraisal.entity.enums.Role;
import com.maulik.appraisal.entity.User;
import com.maulik.appraisal.repository.DepartmentRepository;
import com.maulik.appraisal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            Department hrDept = departmentRepository.save(
                Department.builder().name("Human Resources").description("HR Department").build()
            );

            Department engDept = departmentRepository.save(
                Department.builder().name("Engineering").description("Engineering Department").build()
            );

            User hrUser = User.builder()
                    .fullName("HR Admin")
                    .email("hr@company.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.HR)
                    .department(hrDept)
                    .build();
            userRepository.save(hrUser);

            User manager = User.builder()
                    .fullName("John Manager")
                    .email("manager@company.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.MANAGER)
                    .department(engDept)
                    .build();
            userRepository.save(manager);

            User employee = User.builder()
                    .fullName("Jane Employee")
                    .email("employee@company.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.EMPLOYEE)
                    .department(engDept)
                    .manager(manager)
                    .build();
            userRepository.save(employee);

            log.info("=== Default users seeded ===");
            log.info("HR: hr@company.com / admin123");
            log.info("Manager: manager@company.com / admin123");
            log.info("Employee: employee@company.com / admin123");
        }
    }
}
