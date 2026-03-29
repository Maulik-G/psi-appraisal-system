export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  fullName: string;
  role: string;
  userId: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface UserResponse {
  id: number;
  fullName: string;
  email: string;
  role: string;
  departmentName: string | null;
  departmentId: number | null;
  managerName: string | null;
  managerId: number | null;
  createdAt: string;
}

export interface CreateUserRequest {
  fullName: string;
  email: string;
  password: string;
  role: string;
  departmentId?: number;
  managerId?: number;
}

export interface DepartmentResponse {
  id: number;
  name: string;
  description: string;
  createdAt: string;
}

export interface DepartmentRequest {
  name: string;
  description: string;
}

export interface AppraisalResponse {
  id: number;
  cycleName: string;
  startDate: string;
  endDate: string;
  employeeId: number;
  employeeName: string;
  managerId: number;
  managerName: string;
  status: string;
  selfAssessment: string | null;
  managerComment: string | null;
  rating: number | null;
  createdAt: string;
}

export interface AppraisalRequest {
  cycleName: string;
  startDate: string;
  endDate: string;
  employeeId: number;
  managerId: number;
}

export interface GoalResponse {
  id: number;
  title: string;
  description: string;
  progress: number;
  status: string;
  dueDate: string;
  employeeId: number;
  employeeName: string;
  managerId: number;
  managerName: string;
  createdAt: string;
}

export interface GoalRequest {
  title: string;
  description: string;
  dueDate: string;
  employeeId: number;
}

export interface FeedbackResponse {
  id: number;
  appraisalId: number;
  reviewerId: number;
  reviewerName: string;
  type: string;
  comment: string;
  rating: number | null;
  createdAt: string;
}

export interface FeedbackRequest {
  appraisalId: number;
  type: string;
  comment: string;
  rating?: number;
}

export interface NotificationResponse {
  id: number;
  message: string;
  read: boolean;
  createdAt: string;
}

export type UserRole = 'HR' | 'MANAGER' | 'EMPLOYEE';
