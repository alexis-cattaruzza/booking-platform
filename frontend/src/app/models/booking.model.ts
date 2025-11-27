export interface Appointment {
  id: string;
  appointmentDatetime: string;
  durationMinutes: number;
  price: number;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'NO_SHOW';
  notes: string | null;
  cancellationToken: string;
  service: ServiceInfo;
  customer: CustomerInfo;
  createdAt: string;
}

export interface ServiceInfo {
  id: string;
  name: string;
  durationMinutes: number;
  price: number;
  color: string;
}

export interface CustomerInfo {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
}

export interface AppointmentRequest {
  serviceId: string;
  appointmentDatetime: string;
  customer: CustomerRequest;
  notes?: string;
}

export interface CustomerRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  notes?: string;
}

export interface AvailabilityResponse {
  date: string;
  availableSlots: TimeSlot[];
}

export interface TimeSlot {
  startTime: string;
  endTime: string;
  available: boolean;
}
