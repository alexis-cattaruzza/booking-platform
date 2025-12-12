import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AppointmentService } from '../../services/appointment.service';
import { Appointment, AppointmentStatus } from '../../models/business.model';
import { BusinessCancelModalComponent, AppointmentToCancelDetails } from '../../shared/business-cancel-modal/business-cancel-modal.component';
import { HolidayService, Holiday } from '../../services/holiday.service';

@Component({
  selector: 'app-appointments-management',
  standalone: true,
  imports: [CommonModule, FormsModule, BusinessCancelModalComponent],
  templateUrl: './appointments-management.component.html',
  styleUrl: './appointments-management.component.scss'
})
export class AppointmentsManagementComponent implements OnInit {
  appointments: Appointment[] = [];
  filteredAppointments: Appointment[] = [];
  loading = false;
  error = '';
  success = '';

  // Cancel modal
  isCancelModalOpen = false;
  appointmentToCancel: AppointmentToCancelDetails | null = null;

  // Holidays
  holidays: Holiday[] = [];

  // View mode
  viewMode: 'list' | 'calendar' = 'list';

  // Filters
  selectedFilter: 'all' | 'today' | 'week' | 'upcoming' | 'past' = 'upcoming';
  selectedStatus: AppointmentStatus | 'ALL' = 'ALL';
  searchQuery = '';

  // Calendar view
  currentWeekStart: Date = new Date();
  calendarDays: Date[] = [];
  calendarAppointments: Map<string, Appointment[]> = new Map();

  // Statistics
  stats = {
    total: 0,
    today: 0,
    thisWeek: 0,
    pending: 0,
    confirmed: 0,
    totalRevenue: 0
  };

  statusOptions = [
    { value: 'ALL', label: 'Tous les statuts', color: 'gray' },
    { value: 'PENDING', label: 'En attente', color: 'yellow' },
    { value: 'CONFIRMED', label: 'Confirmé', color: 'blue' },
    { value: 'COMPLETED', label: 'Terminé', color: 'green' },
    { value: 'CANCELLED', label: 'Annulé', color: 'red' },
    { value: 'NO_SHOW', label: 'Absent', color: 'orange' }
  ];

  constructor(
    private appointmentService: AppointmentService,
    private holidayService: HolidayService
  ) {}

  ngOnInit() {
    this.initializeWeek();
    this.loadAppointments();
    this.loadHolidays();
  }

  initializeWeek() {
    const today = new Date();
    const dayOfWeek = today.getDay();
    const monday = new Date(today);
    monday.setDate(today.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1));
    monday.setHours(0, 0, 0, 0);
    this.currentWeekStart = monday;
    this.generateCalendarDays();
  }

  generateCalendarDays() {
    this.calendarDays = [];
    for (let i = 0; i < 7; i++) {
      const day = new Date(this.currentWeekStart);
      day.setDate(this.currentWeekStart.getDate() + i);
      this.calendarDays.push(day);
    }
  }

  previousWeek() {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() - 7);
    this.generateCalendarDays();
    this.updateCalendarAppointments();
  }

  nextWeek() {
    this.currentWeekStart.setDate(this.currentWeekStart.getDate() + 7);
    this.generateCalendarDays();
    this.updateCalendarAppointments();
  }

  goToToday() {
    this.initializeWeek();
    this.updateCalendarAppointments();
  }

  loadAppointments() {
    this.loading = true;
    this.error = '';

    // Get date range (30 days before to 60 days after)
    const start = new Date();
    start.setDate(start.getDate() - 30);
    const end = new Date();
    end.setDate(end.getDate() + 60);

    const startISO = start.toISOString();
    const endISO = end.toISOString();

    this.appointmentService.getAppointments(startISO, endISO).subscribe({
      next: (appointments) => {
        this.appointments = appointments.sort((a, b) =>
          new Date(a.appointmentDatetime).getTime() - new Date(b.appointmentDatetime).getTime()
        );
        this.calculateStatistics();
        this.applyFilters();
        this.updateCalendarAppointments();
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors du chargement des rendez-vous';
        this.loading = false;
      }
    });
  }

  loadHolidays() {
    this.holidayService.getMyHolidays().subscribe({
      next: (holidays) => {
        this.holidays = holidays;
      },
      error: (err) => {
        console.error('Error loading holidays:', err);
        // Don't show error to user, just log it
      }
    });
  }

  calculateStatistics() {
    const now = new Date();
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const todayEnd = new Date(todayStart);
    todayEnd.setDate(todayEnd.getDate() + 1);

    const weekStart = new Date(this.currentWeekStart);
    const weekEnd = new Date(weekStart);
    weekEnd.setDate(weekStart.getDate() + 7);

    this.stats.total = this.appointments.length;
    this.stats.today = this.appointments.filter(apt => {
      const aptDate = new Date(apt.appointmentDatetime);
      return aptDate >= todayStart && aptDate < todayEnd;
    }).length;

    this.stats.thisWeek = this.appointments.filter(apt => {
      const aptDate = new Date(apt.appointmentDatetime);
      return aptDate >= weekStart && aptDate < weekEnd;
    }).length;

    this.stats.pending = this.appointments.filter(apt => apt.status === 'PENDING').length;
    this.stats.confirmed = this.appointments.filter(apt => apt.status === 'CONFIRMED').length;

    this.stats.totalRevenue = this.appointments
      .filter(apt => apt.status === 'COMPLETED')
      .reduce((sum, apt) => sum + apt.price, 0);
  }

  updateCalendarAppointments() {
    this.calendarAppointments.clear();

    this.appointments.forEach(apt => {
      const aptDate = new Date(apt.appointmentDatetime);
      const dateKey = `${aptDate.getFullYear()}-${String(aptDate.getMonth() + 1).padStart(2, '0')}-${String(aptDate.getDate()).padStart(2, '0')}`;

      if (!this.calendarAppointments.has(dateKey)) {
        this.calendarAppointments.set(dateKey, []);
      }
      this.calendarAppointments.get(dateKey)!.push(apt);
    });

    // Sort appointments for each day by time
    this.calendarAppointments.forEach(appointments => {
      appointments.sort((a, b) =>
        new Date(a.appointmentDatetime).getTime() - new Date(b.appointmentDatetime).getTime()
      );
    });
  }

  applyFilters() {
    let filtered = [...this.appointments];

    // Filter by time period
    const now = new Date();
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const todayEnd = new Date(todayStart);
    todayEnd.setDate(todayEnd.getDate() + 1);

    const weekStart = new Date(this.currentWeekStart);
    const weekEnd = new Date(weekStart);
    weekEnd.setDate(weekStart.getDate() + 7);

    if (this.selectedFilter === 'today') {
      filtered = filtered.filter(apt => {
        const aptDate = new Date(apt.appointmentDatetime);
        return aptDate >= todayStart && aptDate < todayEnd;
      });
    } else if (this.selectedFilter === 'week') {
      filtered = filtered.filter(apt => {
        const aptDate = new Date(apt.appointmentDatetime);
        return aptDate >= weekStart && aptDate < weekEnd;
      });
    } else if (this.selectedFilter === 'upcoming') {
      filtered = filtered.filter(apt => new Date(apt.appointmentDatetime) >= now);
    } else if (this.selectedFilter === 'past') {
      filtered = filtered.filter(apt => new Date(apt.appointmentDatetime) < now);
    }

    // Filter by status
    if (this.selectedStatus !== 'ALL') {
      filtered = filtered.filter(apt => apt.status === this.selectedStatus);
    }

    // Filter by search query
    if (this.searchQuery.trim()) {
      const query = this.searchQuery.toLowerCase().trim();
      filtered = filtered.filter(apt =>
        apt.customer.firstName.toLowerCase().includes(query) ||
        apt.customer.lastName.toLowerCase().includes(query) ||
        apt.customer.email.toLowerCase().includes(query) ||
        apt.customer.phone.includes(query) ||
        apt.service.name.toLowerCase().includes(query)
      );
    }

    this.filteredAppointments = filtered;
  }

  onFilterChange(filter: 'all' | 'today' | 'week' | 'upcoming' | 'past') {
    this.selectedFilter = filter;
    this.applyFilters();
  }

  onSearchChange() {
    this.applyFilters();
  }

  onStatusChange(status: AppointmentStatus | 'ALL') {
    this.selectedStatus = status;
    this.applyFilters();
  }

  updateStatus(appointment: Appointment, newStatus: AppointmentStatus) {
    this.appointmentService.updateAppointmentStatus(appointment.id, newStatus).subscribe({
      next: () => {
        this.success = `Statut mis à jour avec succès`;
        appointment.status = newStatus;
        this.applyFilters();
        setTimeout(() => this.success = '', 3000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de la mise à jour du statut';
      }
    });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const options: Intl.DateTimeFormatOptions = {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    };
    return date.toLocaleDateString('fr-FR', options);
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR'
    }).format(price);
  }

  getStatusColor(status: AppointmentStatus): string {
    const statusOption = this.statusOptions.find(opt => opt.value === status);
    return statusOption?.color || 'gray';
  }

  getStatusLabel(status: AppointmentStatus): string {
    const statusOption = this.statusOptions.find(opt => opt.value === status);
    return statusOption?.label || status;
  }

  canConfirm(appointment: Appointment): boolean {
    return appointment.status === 'PENDING';
  }

  canComplete(appointment: Appointment): boolean {
    return appointment.status === 'CONFIRMED' || appointment.status === 'PENDING';
  }

  canCancel(appointment: Appointment): boolean {
    return appointment.status === 'PENDING' || appointment.status === 'CONFIRMED';
  }

  openCancelModal(appointment: Appointment) {
    this.appointmentToCancel = {
      id: appointment.id,
      appointmentDatetime: appointment.appointmentDatetime,
      customer: {
        firstName: appointment.customer.firstName,
        lastName: appointment.customer.lastName
      },
      service: {
        name: appointment.service.name
      }
    };
    this.isCancelModalOpen = true;
  }

  closeCancelModal() {
    this.isCancelModalOpen = false;
    this.appointmentToCancel = null;
  }

  handleCancelConfirm(reason: string) {
    if (!this.appointmentToCancel) return;

    const appointmentId = this.appointmentToCancel.id;

    this.appointmentService.cancelAppointment(appointmentId, reason).subscribe({
      next: () => {
        this.success = 'Rendez-vous annulé avec succès. Un email a été envoyé au client.';

        // Update the appointment status in the list
        const apt = this.appointments.find(a => a.id === appointmentId);
        if (apt) {
          apt.status = 'CANCELLED';
        }

        this.calculateStatistics();
        this.applyFilters();
        this.closeCancelModal();
        setTimeout(() => this.success = '', 5000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de l\'annulation du rendez-vous';
        this.closeCancelModal();
        setTimeout(() => this.error = '', 5000);
      }
    });
  }

  // Keep for backward compatibility, but redirect to modal
  cancelAppointment(appointment: Appointment) {
    this.openCancelModal(appointment);
  }

  // Calendar helper methods
  getDateKey(date: Date): string {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }

  getAppointmentsForDay(date: Date): Appointment[] {
    const key = this.getDateKey(date);
    return this.calendarAppointments.get(key) || [];
  }

  isToday(date: Date): boolean {
    const today = new Date();
    return date.getDate() === today.getDate() &&
           date.getMonth() === today.getMonth() &&
           date.getFullYear() === today.getFullYear();
  }

  formatWeekRange(): string {
    const weekEnd = new Date(this.currentWeekStart);
    weekEnd.setDate(this.currentWeekStart.getDate() + 6);
    const options: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'long', year: 'numeric' };
    return `${this.currentWeekStart.toLocaleDateString('fr-FR', options)} - ${weekEnd.toLocaleDateString('fr-FR', options)}`;
  }

  getDayName(date: Date): string {
    return date.toLocaleDateString('fr-FR', { weekday: 'short' });
  }

  getDayNumber(date: Date): number {
    return date.getDate();
  }

  getMonthName(date: Date): string {
    return date.toLocaleDateString('fr-FR', { month: 'short' });
  }

  toggleViewMode() {
    this.viewMode = this.viewMode === 'list' ? 'calendar' : 'list';
  }

  // Holiday helper methods
  isDateOnHoliday(date: Date): boolean {
    const checkDate = new Date(date);
    checkDate.setHours(0, 0, 0, 0);

    return this.holidays.some(holiday => {
      const startDate = new Date(holiday.startDate);
      const endDate = new Date(holiday.endDate);
      startDate.setHours(0, 0, 0, 0);
      endDate.setHours(0, 0, 0, 0);
      return checkDate >= startDate && checkDate <= endDate;
    });
  }

  getHolidayForDate(date: Date): Holiday | null {
    const checkDate = new Date(date);
    checkDate.setHours(0, 0, 0, 0);

    const holiday = this.holidays.find(holiday => {
      const startDate = new Date(holiday.startDate);
      const endDate = new Date(holiday.endDate);
      startDate.setHours(0, 0, 0, 0);
      endDate.setHours(0, 0, 0, 0);
      return checkDate >= startDate && checkDate <= endDate;
    });

    return holiday || null;
  }

  getActiveHolidaysInfo(): string {
    if (this.holidays.length === 0) return '';

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const activeHolidays = this.holidays.filter(holiday => {
      const startDate = new Date(holiday.startDate);
      const endDate = new Date(holiday.endDate);
      startDate.setHours(0, 0, 0, 0);
      endDate.setHours(0, 0, 0, 0);
      return endDate >= today;
    }).sort((a, b) => new Date(a.startDate).getTime() - new Date(b.startDate).getTime());

    if (activeHolidays.length === 0) return '';

    return activeHolidays.map(holiday => {
      const start = new Date(holiday.startDate).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' });
      const end = new Date(holiday.endDate).toLocaleDateString('fr-FR', { day: 'numeric', month: 'long', year: 'numeric' });
      const reason = holiday.reason || 'Vacances';
      return `${reason}: du ${start} au ${end}`;
    }).join(' | ');
  }
}
