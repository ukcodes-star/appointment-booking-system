import axios from 'axios';
import type { ServiceEntity, ServiceSlotsResponse, UserAppointment, ProviderServiceSchedule } from '../types';

export const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Automatically inject JWT token from localStorage
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Centralized API calls
export const authApi = {
  register: (data: Record<string, unknown>) => api.post('/auth/register', data),
  login: (data: Record<string, unknown>) => api.post<{ token: string }>('/auth/login', data),
};

export const serviceApi = {
  getAll: (type?: string) => api.get<ServiceEntity[]>('/services', { params: { type } }),
  create: (data: { name: string; type: string; durationMinutes: number }) =>
    api.post<ServiceEntity>('/services', data),
  setAvailability: (serviceId: string, data: { dayOfWeek: number; startTime: string; endTime: string }) =>
    api.post(`/services/${serviceId}/availability`, data),
  getSlots: (serviceId: string, date: string) =>
    api.get<ServiceSlotsResponse>(`/services/${serviceId}/slots`, { params: { date } }),
};

export const appointmentApi = {
  book: (slotId: string) => api.post('/appointments', { slotId }),
  getMine: () => api.get<UserAppointment[]>('/appointments/me'),
};

export const providerApi = {
  getSchedule: (date: string) =>
    api.get<ProviderServiceSchedule[]>('/providers/me/schedule', { params: { date } }),
};