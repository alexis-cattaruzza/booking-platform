export interface Business {
  id: string;
  businessName: string;
  slug: string;
  description: string | null;
  address: string | null;
  city: string | null;
  postalCode: string | null;
  phone: string;
  email: string;
  category: string | null;
  logoUrl: string | null;
  settings: any;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateBusinessRequest {
  businessName?: string;
  description?: string;
  category?: string;
  address?: string;
  city?: string;
  postalCode?: string;
  phone?: string;
  email?: string;
  logoUrl?: string;
}

export interface Service {
  id: string;
  name: string;
  description: string | null;
  durationMinutes: number;
  price: number;
  color: string;
  isActive: boolean;
  displayOrder: number;
  createdAt: string;
  updatedAt: string;
}

export interface ServiceRequest {
  name: string;
  description?: string;
  durationMinutes: number;
  price: number;
  color?: string;
  isActive?: boolean;
}

export interface Schedule {
  id: string;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  slotDurationMinutes: number;
  isActive: boolean;
}

export interface ScheduleRequest {
  dayOfWeek: string;
  startTime: string;
  endTime: string;
  slotDurationMinutes: number;
  isActive?: boolean;
}
