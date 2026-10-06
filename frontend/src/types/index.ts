export type Role = 'USER' | 'SERVICE_PROVIDER';

export type ServiceType = 'MEDICAL' | 'HOUSE_HELP' | 'BEAUTY' | 'FITNESS' | 'EDUCATION' | 'OTHER';

export interface User {
  id: string;
  name: string;
  email: string;
  role: Role;
}

export interface ServiceEntity {
  id: string;
  name: string;
  type: ServiceType;
  durationMinutes: number;
  providerName: string;
}

export interface SlotItem {
  slotId: string;
  startTime: string;
  endTime: string;
}

export interface ServiceSlotsResponse {
  serviceId: string;
  date: string;
  slots: SlotItem[];
}

export interface UserAppointment {
  id: string;
  serviceName: string;
  type: ServiceType;
  date: string;
  startTime: string;
  endTime: string;
  status: 'BOOKED' | 'CANCELLED';
}

export interface ProviderAppointmentItem {
  id: string;
  patientName: string;
  date: string;
  startTime: string;
  endTime: string;
  slotId: string;
  status: 'BOOKED' | 'CANCELLED';
}

export interface ProviderServiceSchedule {
  serviceId: string;
  serviceName: string;
  type: ServiceType;
  appointments: ProviderAppointmentItem[];
}