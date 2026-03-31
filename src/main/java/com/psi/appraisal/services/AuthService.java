package com.psi.appraisal.services;

import com.psi.appraisal.dtos.AuthResponse;
import com.psi.appraisal.dtos.LoginRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
}
