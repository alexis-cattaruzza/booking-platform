import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Public endpoints (aligned with backend SecurityConfig)
  const isPublicEndpoint = req.url.includes('/auth/') ||
                           req.url.includes('/booking/') ||
                           req.url.includes('/availability/');

  // Add token to all private endpoints
  if (token && !isPublicEndpoint) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};
