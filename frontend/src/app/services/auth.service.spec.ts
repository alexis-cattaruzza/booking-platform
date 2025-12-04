import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';
import { PLATFORM_ID } from '@angular/core';
import { ApiService } from './api.service';
import { of, throwError } from 'rxjs';
import { AuthResponse } from '../models/auth.model';

describe('AuthService', () => {
  let service: AuthService;
  let store: Record<string, string>;
  let apiSpy: jasmine.SpyObj<ApiService>;

  const mockLoginResponse: AuthResponse = {
    accessToken: 'test-access-token',
    refreshToken: 'test-refresh-token',
    tokenType: 'Bearer',
    user: {
      id: '123',
      email: 'test@example.com',
      firstName: 'John',
      lastName: 'Doe',
      phone: '0612345678',
      role: 'BUSINESS',
      emailVerified: true
    }
  };

  beforeEach(() => {
    store = {};

    // Mock localStorage
    spyOn(window.localStorage, 'getItem').and.callFake((key: string) => store[key] ?? null);
    spyOn(window.localStorage, 'setItem').and.callFake((key: string, value: string) => { store[key] = value; });
    spyOn(window.localStorage, 'removeItem').and.callFake((key: string) => { delete store[key]; });

    // Mock ApiService
    apiSpy = jasmine.createSpyObj('ApiService', ['post', 'get', 'put', 'delete', 'postPublic']);

    // Provide AuthService using factory to pass ApiService and platformId
    TestBed.configureTestingModule({
      providers: [
        {
          provide: AuthService,
          useFactory: () => new AuthService(apiSpy, 'browser'),
        },
        { provide: PLATFORM_ID, useValue: 'browser' }
      ]
    });

    service = TestBed.inject(AuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  // -----------------------------------------------------
  // LOGIN
  // -----------------------------------------------------
  describe('login', () => {
    it('should store tokens on successful login', (done) => {
      const credentials = { email: 'test@example.com', password: 'password123' };
      apiSpy.postPublic.and.returnValue(of(mockLoginResponse));

      service.login(credentials).subscribe((res) => {
        expect(store['accessToken']).toBe('test-access-token');
        expect(store['refreshToken']).toBe('test-refresh-token');
        expect(store['currentUser']).toBeTruthy();
        expect(res.user.email).toBe('test@example.com');
        done();
      });
    });

    it('should handle login error', (done) => {
      const credentials = { email: 'test@example.com', password: 'wrong' };
      apiSpy.postPublic.and.returnValue(throwError({ status: 401 }));

      service.login(credentials).subscribe({
        next: () => done.fail('Should have failed'),
        error: (err) => {
          expect(err.status).toBe(401);
          expect(store['accessToken']).toBeUndefined();
          done();
        }
      });
    });
  });

  // -----------------------------------------------------
  // LOGOUT
  // -----------------------------------------------------
  describe('logout', () => {
    it('should clear tokens', () => {
      store['accessToken'] = 'token';
      store['refreshToken'] = 'refresh';
      store['currentUser'] = JSON.stringify(mockLoginResponse.user);

      service.logout();
      expect(store['accessToken']).toBeUndefined();
      expect(store['refreshToken']).toBeUndefined();
      expect(store['currentUser']).toBeUndefined();
    });
  });

  // -----------------------------------------------------
  // GET TOKEN
  // -----------------------------------------------------
  describe('getToken', () => {
    it('should return token if present', () => {
      store['accessToken'] = 'token';
      expect(service.getToken()).toBe('token');
    });

    it('should return null when missing', () => {
      expect(service.getToken()).toBeNull();
    });
  });

  // -----------------------------------------------------
  // AUTH STATUS
  // -----------------------------------------------------
  describe('isAuthenticated', () => {
    it('should return false if store is empty', () => {
      expect(service.isAuthenticated).toBeFalse();
    });

    it('should return true when store has user', () => {
      store['accessToken'] = 'token';
      store['currentUser'] = JSON.stringify(mockLoginResponse.user);

      service = new AuthService(apiSpy, 'browser');
      expect(service.isAuthenticated).toBeTrue();
    });
  });

  // -----------------------------------------------------
  // CURRENT USER
  // -----------------------------------------------------
  describe('currentUserValue', () => {
    it('should return null if store is empty', () => {
      expect(service.currentUserValue).toBeNull();
    });

    it('should return user when store has user', () => {
      store['accessToken'] = 'token';
      store['currentUser'] = JSON.stringify(mockLoginResponse.user);

      service = new AuthService(apiSpy, 'browser');
      expect(service.currentUserValue?.email).toBe('test@example.com');
    });
  });
});
