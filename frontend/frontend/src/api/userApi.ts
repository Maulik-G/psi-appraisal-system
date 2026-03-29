import api from './axios';
import type { ApiResponse, UserResponse, CreateUserRequest } from './types';

export const userApi = {
  getAll: () => api.get<ApiResponse<UserResponse[]>>('/users'),
  getById: (id: number) => api.get<ApiResponse<UserResponse>>(`/users/${id}`),
  getMe: () => api.get<ApiResponse<UserResponse>>('/users/me'),
  create: (data: CreateUserRequest) => api.post<ApiResponse<UserResponse>>('/users', data),
  getByRole: (role: string) => api.get<ApiResponse<UserResponse[]>>(`/users/role/${role}`),
  getTeam: () => api.get<ApiResponse<UserResponse[]>>('/users/team'),
};
