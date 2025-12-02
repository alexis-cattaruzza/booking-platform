import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Business, Service, UpdateBusinessRequest } from '../models/business.model';

@Injectable({
  providedIn: 'root'
})
export class BusinessService {

  constructor(private api: ApiService) { }

  // My Business endpoints (protected)
  getMyBusiness(): Observable<Business> {
    return this.api.get<Business>('/businesses/me');
  }

  updateMyBusiness(request: UpdateBusinessRequest): Observable<Business> {
    return this.api.put<Business>('/businesses/me', request);
  }

  // Public Business endpoints
  getBusinessBySlug(slug: string): Observable<Business> {
    return this.api.getPublic<Business>(`/businesses/${slug}`);
  }

  getBusinessServices(slug: string): Observable<Service[]> {
    return this.api.getPublic<Service[]>(`/businesses/${slug}/services`);
  }
}
