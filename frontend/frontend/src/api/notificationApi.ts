import api from './axios';
import type { ApiResponse, NotificationResponse } from './types';

export const notificationApi = {
  getAll: () => api.get<ApiResponse<NotificationResponse[]>>('/notifications'),
  getUnreadCount: () => api.get<ApiResponse<{ count: number }>>('/notifications/unread-count'),
  markAsRead: (id: number) => api.put<ApiResponse<void>>(`/notifications/${id}/read`),
  markAllAsRead: () => api.put<ApiResponse<void>>('/notifications/read-all'),
};
