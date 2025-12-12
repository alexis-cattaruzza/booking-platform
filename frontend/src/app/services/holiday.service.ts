import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Holiday {
  id: string;
  businessId: string;
  startDate: string;
  endDate: string;
  reason?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface HolidayRequest {
  startDate: string;
  endDate: string;
  reason?: string;
}

@Injectable({
  providedIn: 'root'
})
export class HolidayService {
  private apiUrl = `${environment.apiUrl}/holidays`;

  constructor(private http: HttpClient) {}

  getMyHolidays(): Observable<Holiday[]> {
    return this.http.get<Holiday[]>(`${this.apiUrl}/me`);
  }

  getUpcomingHolidays(): Observable<Holiday[]> {
    return this.http.get<Holiday[]>(`${this.apiUrl}/me/upcoming`);
  }

  previewAffectedAppointments(startDate: string, endDate: string): Observable<string[]> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.get<string[]>(`${this.apiUrl}/preview`, { params });
  }

  createHoliday(request: HolidayRequest): Observable<Holiday> {
    return this.http.post<Holiday>(this.apiUrl, request);
  }

  updateHoliday(id: string, request: HolidayRequest): Observable<Holiday> {
    return this.http.put<Holiday>(`${this.apiUrl}/${id}`, request);
  }

  deleteHoliday(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getBusinessHolidays(slug: string): Observable<Holiday[]> {
    return this.http.get<Holiday[]>(`${environment.apiUrl}/businesses/${slug}/holidays`);
  }
}
