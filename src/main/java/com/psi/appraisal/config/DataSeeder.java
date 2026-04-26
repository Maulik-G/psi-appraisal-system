package com.psi.appraisal.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.psi.appraisal.entity.Department;
import com.psi.appraisal.entity.User;
import com.psi.appraisal.entity.enums.Role;
import com.psi.appraisal.repository.DepartmentRepository;
import com.psi.appraisal.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

	private final UserRepository userRepository;
	private final DepartmentRepository departmentRepository;
	private final PasswordEncoder passwordEncoder;

	@Bean
	public CommandLineRunner seedData() {
		return args -> {
			// Check if data already seeded
			if (userRepository.count() > 0) {
				log.info("Data already seeded, skipping initialization...");
				return;
			}

			log.info("Starting data seeding...");

			// Seed departments
			Department engineeringDept = seedDepartment("Engineering", "Software Development and IT Operations");
			Department hrDept = seedDepartment("Human Resources", "HR and Recruitment");
			Department salesDept = seedDepartment("Sales", "Business Development and Client Relations");
			Department marketingDept = seedDepartment("Marketing", "Marketing and Brand Management");
			Department financeDept = seedDepartment("Finance", "Financial Planning and Analysis");

			// Seed HR User
			User hrAdmin = seedUser(
				"HR Administrator",
				"hr.admin@psi.com",
				"HrAdmin@1234",
				"HR Manager",
				Role.HR,
				hrDept,
				null
			);

			// Seed Engineering Manager
			User engineeringManager = seedUser(
				"Rahul Sharma",
				"rahul.sharma@psi.com",
				"Manager@1234",
				"Engineering Manager",
				Role.MANAGER,
				engineeringDept,
				null
			);

			// Seed Sales Manager
			User salesManager = seedUser(
				"Priya Patel",
				"priya.patel@psi.com",
				"Manager@1234",
				"Sales Manager",
				Role.MANAGER,
				salesDept,
				null
			);

			// Seed Marketing Manager
			User marketingManager = seedUser(
				"Amit Kumar",
				"amit.kumar@psi.com",
				"Manager@1234",
				"Marketing Manager",
				Role.MANAGER,
				marketingDept,
				null
			);

			// Seed Finance Manager
			User financeManager = seedUser(
				"Neha Gupta",
				"neha.gupta@psi.com",
				"Manager@1234",
				"Finance Manager",
				Role.MANAGER,
				financeDept,
				null
			);

			// Seed Engineering Employees
			seedUser(
				"Arjun Singh",
				"arjun.singh@psi.com",
				"Emp@1234",
				"Senior Software Engineer",
				Role.EMPLOYEE,
				engineeringDept,
				engineeringManager
			);

			seedUser(
				"Sneha Reddy",
				"sneha.reddy@psi.com",
				"Emp@1234",
				"Software Engineer",
				Role.EMPLOYEE,
				engineeringDept,
				engineeringManager
			);

			seedUser(
				"Vikram Patel",
				"vikram.patel@psi.com",
				"Emp@1234",
				"DevOps Engineer",
				Role.EMPLOYEE,
				engineeringDept,
				engineeringManager
			);

			// Seed Sales Employees
			seedUser(
				"Kavya Nair",
				"kavya.nair@psi.com",
				"Emp@1234",
				"Sales Executive",
				Role.EMPLOYEE,
				salesDept,
				salesManager
			);

			seedUser(
				"Rohan Singh",
				"rohan.singh@psi.com",
				"Emp@1234",
				"Sales Executive",
				Role.EMPLOYEE,
				salesDept,
				salesManager
			);

			// Seed Marketing Employees
			seedUser(
				"Pooja Desai",
				"pooja.desai@psi.com",
				"Emp@1234",
				"Marketing Specialist",
				Role.EMPLOYEE,
				marketingDept,
				marketingManager
			);

			// Seed Finance Employees
			seedUser(
				"Aarav Joshi",
				"aarav.joshi@psi.com",
				"Emp@1234",
				"Financial Analyst",
				Role.EMPLOYEE,
				financeDept,
				financeManager
			);

			seedUser(
				"Divya Sharma",
				"divya.sharma@psi.com",
				"Emp@1234",
				"Accountant",
				Role.EMPLOYEE,
				financeDept,
				financeManager
			);

			log.info("Data seeding completed successfully!");
		};
	}

	private Department seedDepartment(String name, String description) {
		if (departmentRepository.existsByName(name)) {
			log.debug("Department {} already exists", name);
			return departmentRepository.findByName(name).orElseThrow();
		}

		Department department = Department.builder()
			.name(name)
			.description(description)
			.build();

		Department saved = departmentRepository.save(department);
		log.info("Created department: {}", name);
		return saved;
	}

	private User seedUser(
		String fullName,
		String email,
		String password,
		String jobTitle,
		Role role,
		Department department,
		User manager
	) {
		if (userRepository.existsByEmail(email)) {
			log.debug("User {} already exists", email);
			return userRepository.findByEmail(email).orElseThrow();
		}

		User user = User.builder()
			.fullName(fullName)
			.email(email)
			.password(passwordEncoder.encode(password))
			.jobTitle(jobTitle)
			.role(role)
			.isActive(true)
			.department(department)
			.manager(manager)
			.build();

		User saved = userRepository.save(user);
		log.info("Created user: {} ({})", fullName, role);
		return saved;
	}
}
