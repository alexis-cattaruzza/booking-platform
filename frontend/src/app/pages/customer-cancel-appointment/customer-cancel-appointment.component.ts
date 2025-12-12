import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { BookingService } from '../../services/booking.service';

interface CustomerAppointmentDetails {
  id: string;
  appointmentDatetime: string;
  status: 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED' | 'NO_SHOW';
  cancellationToken: string;
  service: {
    id: string;
    name: string;
    durationMinutes: number;
    price: number;
  };
  customer: {
    firstName: string;
    lastName: string;
    email: string;
    phone: string;
  };
  business?: {
    businessName: string;
    slug?: string;
    address: string;
    city: string;
    postalCode: string;
    phone: string;
    email: string;
  };
}

@Component({
  selector: 'app-customer-cancel-appointment',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './customer-cancel-appointment.component.html',
  styleUrl: './customer-cancel-appointment.component.scss'
})
export class CustomerCancelAppointmentComponent implements OnInit {
  cancelForm: FormGroup;
  appointment: CustomerAppointmentDetails | null = null;
  loading = false;
  error = '';
  success = false;
  cancellationToken = '';
  selectedReason = '';

  commonReasons = [
    'Empêchement de dernière minute',
    'Imprévu professionnel',
    'Problème de santé',
    'Changement de planning',
    'Conditions météorologiques',
    'Autre raison'
  ];

  constructor(
    private route: ActivatedRoute,
    public router: Router,
    private fb: FormBuilder,
    private bookingService: BookingService
  ) {
    this.cancelForm = this.fb.group({
      reason: ['', Validators.required],
      additionalComment: ['', Validators.maxLength(500)]
    });
  }

  ngOnInit(): void {
    this.cancellationToken = this.route.snapshot.paramMap.get('token') || '';
    if (this.cancellationToken) {
      this.loadAppointment();
    } else {
      this.error = 'Lien d\'annulation invalide';
    }
  }

  selectReason(reason: string): void {
    this.selectedReason = reason;
    this.cancelForm.patchValue({ reason });
  }

  loadAppointment(): void {
    this.loading = true;
    this.error = '';

    this.bookingService.getAppointmentByToken(this.cancellationToken).subscribe({
      next: (appointment: any) => {
        this.appointment = appointment;
        this.loading = false;

        if (appointment.status === 'CANCELLED') {
          this.success = true;
        }
      },
      error: (err) => {
        this.error = err.error?.message || 'Rendez-vous introuvable. Le lien d\'annulation est peut-être expiré.';
        this.loading = false;
      }
    });
  }

  canCancel(): boolean {
    if (!this.appointment) return false;
    if (this.appointment.status === 'CANCELLED') return false;
    if (this.appointment.status === 'COMPLETED') return false;

    const appointmentDate = new Date(this.appointment.appointmentDatetime);
    if (appointmentDate < new Date()) return false;

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

  onSubmit(): void {
    if (this.cancelForm.invalid || !this.appointment || !this.canCancel()) {
      this.cancelForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.error = '';

    const reason = this.cancelForm.get('reason')?.value;
    const comment = this.cancelForm.get('additionalComment')?.value;
    const cancellationReason = comment ? `${reason} - ${comment}` : reason;

    this.bookingService.cancelAppointment(this.cancellationToken, cancellationReason).subscribe({
      next: () => {
        this.success = true;
        this.loading = false;
        if (this.appointment) {
          this.appointment.status = 'CANCELLED';
        }
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de l\'annulation. Veuillez réessayer ou contacter l\'établissement.';
        this.loading = false;
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

  formatPrice(price: number): string {
    return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(price);
  }

  getBookingUrl(): string {
    if (this.appointment?.business?.slug) {
      return `/book/${this.appointment.business.slug}`;
    }
    return '/';
  }
}
