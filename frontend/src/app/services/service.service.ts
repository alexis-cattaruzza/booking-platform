import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Service, ServiceRequest } from '../models/business.model';

@Injectable({
  providedIn: 'root'
})
export class ServiceService {

  constructor(private api: ApiService) { }

  getMyServices(): Observable<Service[]> {
    return this.api.get<Service[]>('/services');
  }

  getServiceById(id: string): Observable<Service> {
    return this.api.get<Service>(`/services/${id}`);
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
}
