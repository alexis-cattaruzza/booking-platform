import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from './api.service';
import { Business, UpdateBusinessRequest } from '../models/business.model';

@Injectable({
  providedIn: 'root'
})
export class BusinessService {

  constructor(private api: ApiService) { }

  getMyBusiness(): Observable<Business> {
    return this.api.get<Business>('/businesses/me');
  }

  updateMyBusiness(request: UpdateBusinessRequest): Observable<Business> {
    return this.api.put<Business>('/businesses/me', request);
  }

  getBusinessBySlug(slug: string): Observable<Business> {
    return this.api.get<Business>(`/businesses/${slug}`);
  }
}
