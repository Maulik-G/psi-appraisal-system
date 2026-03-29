import api from './axios';
import type { ApiResponse, FeedbackResponse, FeedbackRequest } from './types';

export const feedbackApi = {
  create: (data: FeedbackRequest) => api.post<ApiResponse<FeedbackResponse>>('/feedback', data),
  getByAppraisal: (appraisalId: number) =>
    api.get<ApiResponse<FeedbackResponse[]>>(`/feedback/appraisal/${appraisalId}`),
};
