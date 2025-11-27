import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Skip auth for public endpoints
  const isPublicEndpoint = req.url.includes('/auth/') ||
                           req.url.includes('/booking/') ||
                           req.url.includes('/availability/') ||
                           req.url.includes('/businesses/') && req.method === 'GET';

  if (token && !isPublicEndpoint) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};
