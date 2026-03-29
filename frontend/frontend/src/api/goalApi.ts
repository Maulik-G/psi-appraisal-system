import api from './axios';
import type { ApiResponse, GoalResponse, GoalRequest } from './types';

export const goalApi = {
  getAll: () => api.get<ApiResponse<GoalResponse[]>>('/goals'),
  getMy: () => api.get<ApiResponse<GoalResponse[]>>('/goals/my'),
  getTeam: () => api.get<ApiResponse<GoalResponse[]>>('/goals/team'),
  create: (data: GoalRequest) => api.post<ApiResponse<GoalResponse>>('/goals', data),
  updateProgress: (id: number, progress: number, status?: string) =>
    api.put<ApiResponse<GoalResponse>>(`/goals/${id}/progress`, { progress, status }),
};
