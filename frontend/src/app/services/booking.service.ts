import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  Appointment,
  AppointmentRequest,
  AvailabilityResponse
} from '../models/booking.model';

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  constructor(private api: ApiService) {}

  // Appointments endpoints (protected - for business dashboard)
  getAppointments(start: string, end: string): Observable<Appointment[]> {
    return this.api.get<Appointment[]>('/appointments', { start, end });
  }

  updateAppointmentStatus(id: string, status: string): Observable<Appointment> {
    return this.api.put<Appointment>(`/appointments/${id}/status`, null);
  }

  // Public booking endpoints (for customers)
  getAvailability(businessSlug: string, serviceId: string, date: string): Observable<AvailabilityResponse> {
    return this.api.getPublic<AvailabilityResponse>(`/availability/${businessSlug}`, {
      serviceId,
      date
    });
  }

  createBooking(businessSlug: string, request: AppointmentRequest): Observable<Appointment> {
    return this.api.postPublic<Appointment>(`/booking/${businessSlug}`, request);
  }

  getAppointmentByToken(token: string): Observable<Appointment> {
    return this.api.getPublic<Appointment>(`/booking/appointment/${token}`);
  }

  cancelAppointment(token: string, cancellationReason: string): Observable<void> {
    return this.api.postPublic<void>(`/booking/cancel/${token}`, { cancellationReason });
  }
}
