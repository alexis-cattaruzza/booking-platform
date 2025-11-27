import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import {
  Appointment,
  AppointmentRequest,
  AvailabilityResponse
} from '../models/booking.model';
import { Business, Service, Schedule, ServiceRequest, ScheduleRequest, UpdateBusinessRequest } from '../models/business.model';

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  constructor(private api: ApiService) {}

  // Business endpoints
  getMyBusiness(): Observable<Business> {
    return this.api.get<Business>('/businesses/me');
  }

  updateMyBusiness(request: UpdateBusinessRequest): Observable<Business> {
    return this.api.put<Business>('/businesses/me', request);
  }

  getBusinessBySlug(slug: string): Observable<Business> {
    return this.api.getPublic<Business>(`/businesses/${slug}`);
  }

  // Services endpoints
  getServices(): Observable<Service[]> {
    return this.api.get<Service[]>('/services');
  }

  createService(request: ServiceRequest): Observable<Service> {
    return this.api.post<Service>('/services', request);
  }

  updateService(id: string, request: ServiceRequest): Observable<Service> {
    return this.api.put<Service>(`/services/${id}`, request);
  }

  deleteService(id: string): Observable<void> {
    return this.api.delete<void>(`/services/${id}`);
  }

  // Schedules endpoints
  getSchedules(): Observable<Schedule[]> {
    return this.api.get<Schedule[]>('/schedules');
  }

  createOrUpdateSchedule(request: ScheduleRequest): Observable<Schedule> {
    return this.api.post<Schedule>('/schedules', request);
  }

  deleteSchedule(id: string): Observable<void> {
    return this.api.delete<void>(`/schedules/${id}`);
  }

  // Appointments endpoints
  getAppointments(start: string, end: string): Observable<Appointment[]> {
    return this.api.get<Appointment[]>('/appointments', { start, end });
  }

  updateAppointmentStatus(id: string, status: string): Observable<Appointment> {
    return this.api.put<Appointment>(`/appointments/${id}/status`, null, );
  }

  // Public booking endpoints
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

  cancelAppointment(token: string): Observable<void> {
    return this.api.postPublic<void>(`/booking/cancel/${token}`, {});
  }
}
