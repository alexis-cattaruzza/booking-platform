import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

/**
 * Authentication interceptor
 * Adds JWT token to protected endpoints only
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();

  // Define PUBLIC endpoints explicitly (whitelist approach for security)
  const publicEndpoints = [
    '/api/auth/login',
    '/api/auth/register',
    '/api/booking/',           // Customer booking creation
    '/api/availability/',       // Check availability
  ];

  // Check if the request is to a public endpoint
  const isPublicEndpoint = publicEndpoints.some(endpoint => req.url.includes(endpoint));

  // Special case: /api/businesses/{slug} and /api/businesses/{slug}/services are public
  // but /api/businesses/me, /api/businesses/gdpr/**, and /api/businesses/change-password are PROTECTED
  const isPublicBusinessEndpoint = req.url.includes('/api/businesses/') &&
                                    !req.url.includes('/api/businesses/me') &&
                                    !req.url.includes('/api/businesses/gdpr') &&
                                    !req.url.includes('/api/businesses/change-password') &&
                                    !req.url.includes('/api/businesses/cancel-deletion') &&
                                    !req.url.endsWith('/api/businesses');

  // Add token to all PROTECTED endpoints (default behavior for security)
  if (token && !isPublicEndpoint && !isPublicBusinessEndpoint) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }

  return next(req);
};
