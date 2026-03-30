package com.maulik.appraisal.dto;

import com.maulik.appraisal.entity.enums.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private Role role;
}