import api from './axios';
import type { ApiResponse, AppraisalResponse, AppraisalRequest } from './types';

export const appraisalApi = {
  getAll: () => api.get<ApiResponse<AppraisalResponse[]>>('/appraisals'),
  getById: (id: number) => api.get<ApiResponse<AppraisalResponse>>(`/appraisals/${id}`),
  getMy: () => api.get<ApiResponse<AppraisalResponse[]>>('/appraisals/my'),
  getTeam: () => api.get<ApiResponse<AppraisalResponse[]>>('/appraisals/team'),
  create: (data: AppraisalRequest) => api.post<ApiResponse<AppraisalResponse>>('/appraisals', data),
  submitSelfAssessment: (id: number, selfAssessment: string) =>
    api.put<ApiResponse<AppraisalResponse>>(`/appraisals/${id}/self-assess`, { selfAssessment }),
  managerReview: (id: number, managerComment: string, rating: number) =>
    api.put<ApiResponse<AppraisalResponse>>(`/appraisals/${id}/manager-review`, { managerComment, rating }),
  approve: (id: number) => api.put<ApiResponse<AppraisalResponse>>(`/appraisals/${id}/approve`),
  acknowledge: (id: number) => api.put<ApiResponse<AppraisalResponse>>(`/appraisals/${id}/acknowledge`),
};
