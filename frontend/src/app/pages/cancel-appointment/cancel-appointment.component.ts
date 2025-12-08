import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { BookingService } from '../../services/booking.service';
import { Appointment } from '../../models/booking.model';

@Component({
  selector: 'app-cancel-appointment',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cancel-appointment.component.html'
})
export class CancelAppointmentComponent implements OnInit {
  token: string = '';
  appointment: Appointment | null = null;
  loading = true;
  error: string | null = null;
  cancelling = false;
  cancelled = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private bookingService: BookingService
  ) {}

  ngOnInit() {
    this.token = this.route.snapshot.paramMap.get('token') || '';
    if (!this.token) {
      this.error = 'Token d\'annulation invalide';
      this.loading = false;
      return;
    }

    this.loadAppointment();
  }

  loadAppointment() {
    this.loading = true;
    this.error = null;

    this.bookingService.getAppointmentByToken(this.token).subscribe({
      next: (appointment) => {
        this.appointment = appointment;
        this.loading = false;

        // Check if already cancelled
        if (appointment.status === 'CANCELLED') {
          this.cancelled = true;
        }
      },
      error: (err) => {
        console.error('Error loading appointment:', err);
        this.error = 'Impossible de charger les informations du rendez-vous. Le lien est peut-être invalide ou expiré.';
        this.loading = false;
      }
    });
  }

  canCancel(): boolean {
    if (!this.appointment) return false;
    if (this.appointment.status === 'CANCELLED') return false;
    if (this.appointment.status === 'COMPLETED') return false;

    // Check if appointment is in the past
    const appointmentDate = new Date(this.appointment.appointmentDatetime);
    if (appointmentDate < new Date()) return false;

    // Check if within 3 days (72 hours)
    const cancellationDeadline = new Date(appointmentDate);
    cancellationDeadline.setDate(cancellationDeadline.getDate() - 3);

    return new Date() <= cancellationDeadline;
  }

  getCancellationDeadline(): Date | null {
    if (!this.appointment) return null;
    const appointmentDate = new Date(this.appointment.appointmentDatetime);
    const deadline = new Date(appointmentDate);
    deadline.setDate(deadline.getDate() - 3);
    return deadline;
  }

  confirmCancellation() {
    if (!this.canCancel()) {
      return;
    }

    if (!confirm('Êtes-vous sûr de vouloir annuler ce rendez-vous ?')) {
      return;
    }

    this.cancelling = true;
    this.error = null;

    this.bookingService.cancelAppointment(this.token).subscribe({
      next: () => {
        this.cancelled = true;
        this.cancelling = false;
        if (this.appointment) {
          this.appointment.status = 'CANCELLED';
        }
      },
      error: (err) => {
        console.error('Error cancelling appointment:', err);
        this.error = err.error?.message ||
          'Une erreur est survenue lors de l\'annulation. Veuillez réessayer ou contacter l\'établissement.';
        this.cancelling = false;
      }
    });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
