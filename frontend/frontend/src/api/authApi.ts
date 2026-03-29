import api from './axios';
import type { ApiResponse, LoginRequest, LoginResponse } from './types';

export const authApi = {
  login: (data: LoginRequest) => api.post<ApiResponse<LoginResponse>>('/auth/login', data),
};
