import api from './axios';
import type { ApiResponse, DepartmentResponse, DepartmentRequest } from './types';

export const departmentApi = {
  getAll: () => api.get<ApiResponse<DepartmentResponse[]>>('/departments'),
  getById: (id: number) => api.get<ApiResponse<DepartmentResponse>>(`/departments/${id}`),
  create: (data: DepartmentRequest) => api.post<ApiResponse<DepartmentResponse>>('/departments', data),
  update: (id: number, data: DepartmentRequest) => api.put<ApiResponse<DepartmentResponse>>(`/departments/${id}`, data),
  delete: (id: number) => api.delete<ApiResponse<void>>(`/departments/${id}`),
};
