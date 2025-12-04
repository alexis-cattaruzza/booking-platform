import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { AppointmentService } from './appointment.service';
import { Appointment } from '../models/business.model';

describe('AppointmentService', () => {
  let service: AppointmentService;
  let httpMock: HttpTestingController;

  const mockAppointment: Appointment = {
    id: '123',
    appointmentDatetime: '2025-12-10T14:00:00',
    durationMinutes: 30,
    price: 30.0,
    status: 'PENDING',
    notes: 'First visit',
    cancellationToken: 'token-123',
    service: {
      id: 'service-123',
      name: 'Haircut',
      durationMinutes: 30,
      price: 30.0,
      color: '#3B82F6'
    },
    customer: {
      id: 'customer-123',
      firstName: 'John',
      lastName: 'Doe',
      email: 'john@example.com',
      phone: '+33612345678'
    },
    createdAt: '2025-12-01T10:00:00'
  };

  const mockAppointments: Appointment[] = [mockAppointment];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AppointmentService]
    });

    service = TestBed.inject(AppointmentService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getAppointments', () => {
    it('should fetch appointments for date range', (done) => {
      const start = '2025-12-01';
      const end = '2025-12-31';

      service.getAppointments(start, end).subscribe({
        next: (appointments: Appointment[]) => {
          expect(appointments).toEqual(mockAppointments);
          expect(appointments.length).toBe(1);
          done();
        },
        error: done.fail
      });

      const req = httpMock.expectOne(req => 
        req.urlWithParams.includes(`/appointments?start=${start}&end=${end}`)
      );
      expect(req.request.method).toBe('GET');
      req.flush(mockAppointments);
    });

    it('should handle empty appointments list', (done) => {
      const start = '2025-12-01';
      const end = '2025-12-31';

      service.getAppointments(start, end).subscribe({
        next: (appointments: Appointment[]) => {
          expect(appointments).toEqual([]);
          expect(appointments.length).toBe(0);
          done();
        },
        error: done.fail
      });

      const req = httpMock.expectOne(req =>
        req.urlWithParams.includes(`/appointments?start=${start}&end=${end}`)
      );
      req.flush([]);
    });

    it('should handle server error', (done) => {
      const start = '2025-12-01';
      const end = '2025-12-31';

      service.getAppointments(start, end).subscribe({
        next: () => done.fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(500);
          done();
        }
      });

      const req = httpMock.expectOne(req =>
        req.urlWithParams.includes(`/appointments?start=${start}&end=${end}`)
      );
      req.flush('Server error', { status: 500, statusText: 'Internal Server Error' });
    });
  });

  describe('updateAppointmentStatus', () => {
    it('should update appointment status to CONFIRMED', (done) => {
      const appointmentId = '123';
      const newStatus = 'CONFIRMED';

      service.updateAppointmentStatus(appointmentId, newStatus).subscribe({
        next: (appointment: Appointment) => {
          expect(appointment.status).toBe(newStatus);
          done();
        },
        error: done.fail
      });

      const req = httpMock.expectOne(req =>
        req.urlWithParams.includes(`/appointments/${appointmentId}/status?status=${newStatus}`)
      );
      expect(req.request.method).toBe('PUT');
      req.flush({ ...mockAppointment, status: newStatus });
    });

    it('should handle unauthorized status update', (done) => {
      const appointmentId = '123';
      const newStatus = 'CONFIRMED';

      service.updateAppointmentStatus(appointmentId, newStatus).subscribe({
        next: () => done.fail('should have failed'),
        error: (error: any) => {
          expect(error.status).toBe(403);
          done();
        }
      });

      const req = httpMock.expectOne(req =>
        req.urlWithParams.includes(`/appointments/${appointmentId}/status?status=${newStatus}`)
      );
      req.flush('Forbidden', { status: 403, statusText: 'Forbidden' });
    });
  });
});
