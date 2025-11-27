import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Schedule, ScheduleRequest } from '../models/business.model';

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {

  constructor(private api: ApiService) { }

  getMySchedules(): Observable<Schedule[]> {
    return this.api.get<Schedule[]>('/schedules');
  }

  getScheduleById(id: string): Observable<Schedule> {
    return this.api.get<Schedule>(`/schedules/${id}`);
  }

  createOrUpdateSchedule(request: ScheduleRequest): Observable<Schedule> {
    return this.api.post<Schedule>('/schedules', request);
  }

  updateSchedule(id: string, request: ScheduleRequest): Observable<Schedule> {
    return this.api.put<Schedule>(`/schedules/${id}`, request);
  }

  deleteSchedule(id: string): Observable<void> {
    return this.api.delete<void>(`/schedules/${id}`);
  }
}
