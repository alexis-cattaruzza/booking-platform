import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { HolidayService, Holiday, HolidayRequest } from '../../services/holiday.service';

@Component({
  selector: 'app-vacation-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './vacation-management.component.html',
  styleUrl: './vacation-management.component.scss'
})
export class VacationManagementComponent implements OnInit {
  holidays: Holiday[] = [];
  holidayForm: FormGroup;
  affectedAppointments: string[] = [];
  showPreview = false;
  loading = false;
  error = '';
  success = '';
  editingHolidayId: string | null = null;

  constructor(
    private fb: FormBuilder,
    private holidayService: HolidayService
  ) {
    this.holidayForm = this.fb.group({
      startDate: ['', Validators.required],
      endDate: ['', Validators.required],
      reason: ['', Validators.maxLength(500)]
    });
  }

  ngOnInit() {
    this.loadHolidays();
  }

  loadHolidays() {
    this.holidayService.getMyHolidays().subscribe({
      next: (holidays) => {
        this.holidays = holidays.sort((a, b) =>
          new Date(a.startDate).getTime() - new Date(b.startDate).getTime()
        );
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors du chargement des vacances';
      }
    });
  }

  onDatesChange() {
    const startDate = this.holidayForm.get('startDate')?.value;
    const endDate = this.holidayForm.get('endDate')?.value;

    if (startDate && endDate) {
      this.previewAffectedAppointments(startDate, endDate);
    }
  }

  previewAffectedAppointments(startDate: string, endDate: string) {
    this.holidayService.previewAffectedAppointments(startDate, endDate).subscribe({
      next: (appointments) => {
        this.affectedAppointments = appointments;
        this.showPreview = appointments.length > 0;
      },
      error: () => {
        this.affectedAppointments = [];
        this.showPreview = false;
      }
    });
  }

  onSubmit() {
    if (this.holidayForm.invalid) return;

    this.loading = true;
    this.error = '';
    this.success = '';

    const request: HolidayRequest = this.holidayForm.value;

    const action = this.editingHolidayId
      ? this.holidayService.updateHoliday(this.editingHolidayId, request)
      : this.holidayService.createHoliday(request);

    action.subscribe({
      next: () => {
        this.success = this.editingHolidayId
          ? 'Vacances modifiées avec succès'
          : 'Vacances créées avec succès. Les rendez-vous pendant cette période ont été annulés.';
        this.loading = false;
        this.resetForm();
        this.loadHolidays();
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de la sauvegarde des vacances';
        this.loading = false;
      }
    });
  }

  editHoliday(holiday: Holiday) {
    this.editingHolidayId = holiday.id;
    this.holidayForm.patchValue({
      startDate: holiday.startDate,
      endDate: holiday.endDate,
      reason: holiday.reason
    });
    this.onDatesChange();
  }

  deleteHoliday(id: string) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette période de vacances ?')) {
      return;
    }

    this.holidayService.deleteHoliday(id).subscribe({
      next: () => {
        this.success = 'Vacances supprimées avec succès';
        this.loadHolidays();
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de la suppression';
      }
    });
  }

  resetForm() {
    this.holidayForm.reset();
    this.editingHolidayId = null;
    this.showPreview = false;
    this.affectedAppointments = [];
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

  isUpcoming(startDate: string): boolean {
    return new Date(startDate) >= new Date();
  }

  isPast(endDate: string): boolean {
    return new Date(endDate) < new Date();
  }

  isCurrent(startDate: string, endDate: string): boolean {
    const now = new Date();
    return new Date(startDate) <= now && new Date(endDate) >= now;
  }
}
