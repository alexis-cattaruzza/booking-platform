import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Appointment, AppointmentStatus } from '../models/business.model';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {

  constructor(private api: ApiService) { }

  getAppointments(start: string, end: string): Observable<Appointment[]> {
    return this.api.get<Appointment[]>(`/appointments?start=${start}&end=${end}`);
  }

  updateAppointmentStatus(appointmentId: string, status: AppointmentStatus): Observable<Appointment> {
    return this.api.put<Appointment>(`/appointments/${appointmentId}/status?status=${status}`, {});
  }

  cancelAppointment(appointmentId: string, cancellationReason: string): Observable<void> {
    return this.api.post<void>(`/appointments/${appointmentId}/cancel`, { cancellationReason });
  }
}
