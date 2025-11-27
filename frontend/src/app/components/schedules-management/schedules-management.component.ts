import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ScheduleService } from '../../services/schedule.service';
import { Schedule, ScheduleRequest } from '../../models/business.model';

@Component({
  selector: 'app-schedules-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './schedules-management.component.html',
  styleUrl: './schedules-management.component.scss'
})
export class SchedulesManagementComponent implements OnInit {
  schedules: Schedule[] = [];
  scheduleForm: FormGroup;
  loading = false;
  saving = false;
  error = '';
  success = '';

  editingSchedule: Schedule | null = null;
  showForm = false;

  daysOfWeek = [
    { value: 'MONDAY', label: 'Lundi', order: 1 },
    { value: 'TUESDAY', label: 'Mardi', order: 2 },
    { value: 'WEDNESDAY', label: 'Mercredi', order: 3 },
    { value: 'THURSDAY', label: 'Jeudi', order: 4 },
    { value: 'FRIDAY', label: 'Vendredi', order: 5 },
    { value: 'SATURDAY', label: 'Samedi', order: 6 },
    { value: 'SUNDAY', label: 'Dimanche', order: 0 }
  ];

  constructor(
    private fb: FormBuilder,
    private scheduleService: ScheduleService
  ) {
    this.scheduleForm = this.fb.group({
      dayOfWeek: ['', Validators.required],
      startTime: ['09:00', Validators.required],
      endTime: ['17:00', Validators.required],
      slotDurationMinutes: [30, [Validators.required, Validators.min(5), Validators.max(240)]],
      isActive: [true]
    });
  }

  ngOnInit() {
    this.loadSchedules();
  }

  loadSchedules() {
    this.loading = true;
    this.error = '';

    this.scheduleService.getMySchedules().subscribe({
      next: (schedules) => {
        this.schedules = this.sortSchedulesByDay(schedules);
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors du chargement des horaires';
        this.loading = false;
      }
    });
  }

  sortSchedulesByDay(schedules: Schedule[]): Schedule[] {
    const dayOrder: { [key: string]: number } = {
      'MONDAY': 1, 'TUESDAY': 2, 'WEDNESDAY': 3, 'THURSDAY': 4,
      'FRIDAY': 5, 'SATURDAY': 6, 'SUNDAY': 0
    };
    return schedules.sort((a, b) => dayOrder[a.dayOfWeek] - dayOrder[b.dayOfWeek]);
  }

  openCreateForm() {
    this.editingSchedule = null;
    this.scheduleForm.reset({
      dayOfWeek: '',
      startTime: '09:00',
      endTime: '17:00',
      slotDurationMinutes: 30,
      isActive: true
    });
    this.showForm = true;
  }

  openEditForm(schedule: Schedule) {
    this.editingSchedule = schedule;
    this.scheduleForm.patchValue({
      dayOfWeek: schedule.dayOfWeek,
      startTime: schedule.startTime,
      endTime: schedule.endTime,
      slotDurationMinutes: schedule.slotDurationMinutes,
      isActive: schedule.isActive
    });
    this.showForm = true;
  }

  cancelForm() {
    this.showForm = false;
    this.editingSchedule = null;
    this.scheduleForm.reset();
  }

  onSubmit() {
    if (this.scheduleForm.invalid) {
      return;
    }

    this.saving = true;
    this.error = '';
    this.success = '';

    const request: ScheduleRequest = this.scheduleForm.value;

    const operation = this.editingSchedule
      ? this.scheduleService.updateSchedule(this.editingSchedule.id, request)
      : this.scheduleService.createOrUpdateSchedule(request);

    operation.subscribe({
      next: () => {
        this.success = this.editingSchedule
          ? 'Horaire mis à jour avec succès !'
          : 'Horaire créé avec succès !';
        this.saving = false;
        this.showForm = false;
        this.editingSchedule = null;
        this.loadSchedules();
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de l\'enregistrement de l\'horaire';
        this.saving = false;
      }
    });
  }

  deleteSchedule(schedule: Schedule) {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer l'horaire du ${this.getDayLabel(schedule.dayOfWeek)} ?`)) {
      return;
    }

    this.scheduleService.deleteSchedule(schedule.id).subscribe({
      next: () => {
        this.success = 'Horaire supprimé avec succès !';
        this.loadSchedules();
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de la suppression de l\'horaire';
      }
    });
  }

  getDayLabel(dayOfWeek: string): string {
    return this.daysOfWeek.find(d => d.value === dayOfWeek)?.label || dayOfWeek;
  }

  getAvailableDays(): typeof this.daysOfWeek {
    if (this.editingSchedule) {
      return this.daysOfWeek;
    }
    const usedDays = this.schedules.map(s => s.dayOfWeek);
    return this.daysOfWeek.filter(d => !usedDays.includes(d.value));
  }
}
