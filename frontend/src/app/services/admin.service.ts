import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface AuditLog {
  id: number;
  username: string;
  action: string;
  resourceType?: string;
  resourceId?: string;
  ipAddress: string;
  userAgent: string;
  status: 'SUCCESS' | 'FAILURE' | 'ERROR';
  errorMessage?: string;
  createdAt: string;
}

export interface AuditLogPage {
  content: AuditLog[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface Business {
  id: string;
  businessName: string;
  slug: string;
  category: string;
  description?: string;
  address?: string;
  city?: string;
  postalCode?: string;
  email?: string;
  phone?: string;
  status: 'ACTIVE' | 'SUSPENDED' | 'DELETED';
  ownerFirstName: string;
  ownerLastName: string;
  createdAt: string;
  deletedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  // Audit Logs
  getAuditLogs(page: number = 0, size: number = 20): Observable<AuditLogPage> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<AuditLogPage>(`${this.apiUrl}/audit`, { params });
  }

  getAuditLogsByUser(userId: string, page: number = 0, size: number = 20): Observable<AuditLogPage> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<AuditLogPage>(`${this.apiUrl}/audit/user/${userId}`, { params });
  }

  getAuditLogsByAction(action: string, page: number = 0, size: number = 20): Observable<AuditLogPage> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<AuditLogPage>(`${this.apiUrl}/audit/action/${action}`, { params });
  }

  getAuditLogsByStatus(status: string, page: number = 0, size: number = 20): Observable<AuditLogPage> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<AuditLogPage>(`${this.apiUrl}/audit/status/${status}`, { params });
  }

  getAuditLogsByDateRange(startDate: string, endDate: string, page: number = 0, size: number = 20): Observable<AuditLogPage> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<AuditLogPage>(`${this.apiUrl}/audit/date-range`, { params });
  }

  // Business Management
  getAllBusinesses(page: number = 0, size: number = 20): Observable<any> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<any>(`${this.apiUrl}/businesses`, { params });
  }

  suspendBusiness(businessId: string): Observable<Business> {
    return this.http.put<Business>(`${this.apiUrl}/businesses/${businessId}/suspend`, {});
  }

  activateBusiness(businessId: string): Observable<Business> {
    return this.http.put<Business>(`${this.apiUrl}/businesses/${businessId}/activate`, {});
  }

  deleteBusiness(businessId: string): Observable<Business> {
    return this.http.delete<Business>(`${this.apiUrl}/businesses/${businessId}`);
  }

  // User management methods would go here
}
