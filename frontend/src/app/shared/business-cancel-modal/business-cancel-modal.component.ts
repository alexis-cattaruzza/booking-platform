import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

export interface AppointmentToCancelDetails {
  id: string;
  appointmentDatetime: string;
  customer: {
    firstName: string;
    lastName: string;
  };
  service: {
    name: string;
  };
}

@Component({
  selector: 'app-business-cancel-modal',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './business-cancel-modal.component.html',
  styleUrl: './business-cancel-modal.component.scss'
})
export class BusinessCancelModalComponent {
  @Input() appointment: AppointmentToCancelDetails | null = null;
  @Input() isOpen = false;
  @Output() closeModal = new EventEmitter<void>();
  @Output() confirmCancel = new EventEmitter<string>();

  cancelForm: FormGroup;
  selectedReason: string = '';

  cancellationReasons = [
    'Client n\'est pas venu',
    'Demande du client',
    'Problème technique',
    'Indisponibilité du personnel',
    'Erreur de réservation',
    'Fermeture exceptionnelle',
    'Autre raison'
  ];

  constructor(private fb: FormBuilder) {
    this.cancelForm = this.fb.group({
      reason: ['', Validators.required],
      additionalComment: ['', Validators.maxLength(500)]
    });
  }

  selectReason(reason: string): void {
    this.selectedReason = reason;
    this.cancelForm.patchValue({ reason });
  }

  onClose(): void {
    this.closeModal.emit();
    this.resetForm();
  }

  onConfirm(): void {
    if (this.cancelForm.invalid) {
      this.cancelForm.markAllAsTouched();
      return;
    }

    const reason = this.cancelForm.get('reason')?.value;
    const comment = this.cancelForm.get('additionalComment')?.value;
    const finalReason = comment ? `${reason} - ${comment}` : reason;

    this.confirmCancel.emit(finalReason);
    this.resetForm();
  }

  resetForm(): void {
    this.selectedReason = '';
    this.cancelForm.reset();
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
