import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { BookingService } from '../../services/booking.service';
import { BusinessService } from '../../services/business.service';
import { Business, Service } from '../../models/business.model';
import { AppointmentRequest, AvailabilityResponse, TimeSlot } from '../../models/booking.model';

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './booking.component.html',
  styleUrl: './booking.component.scss'
})
export class BookingComponent implements OnInit {
  business: Business | null = null;
  services: Service[] = [];
  selectedService: Service | null = null;
  selectedDate: Date | null = null;
  selectedTimeSlot: TimeSlot | null = null;
  availableSlots: TimeSlot[] = [];

  customerForm: FormGroup;
  loading = false;
  loadingAvailability = false;
  error = '';
  success = '';

  step: 'service' | 'datetime' | 'customer' | 'confirmation' = 'service';
  slug = '';

  // Calendar
  currentMonth: Date = new Date();
  calendarDays: (Date | null)[] = [];

  // Phone countries
  countries = [
    { code: 'FR', name: 'France', dialCode: '+33' },
    { code: 'BE', name: 'Belgique', dialCode: '+32' },
    { code: 'CH', name: 'Suisse', dialCode: '+41' },
    { code: 'LU', name: 'Luxembourg', dialCode: '+352' },
    { code: 'CA', name: 'Canada', dialCode: '+1' },
    { code: 'US', name: 'États-Unis', dialCode: '+1' },
    { code: 'GB', name: 'Royaume-Uni', dialCode: '+44' },
    { code: 'DE', name: 'Allemagne', dialCode: '+49' },
    { code: 'ES', name: 'Espagne', dialCode: '+34' },
    { code: 'IT', name: 'Italie', dialCode: '+39' },
    { code: 'PT', name: 'Portugal', dialCode: '+351' },
    { code: 'NL', name: 'Pays-Bas', dialCode: '+31' },
    { code: 'MA', name: 'Maroc', dialCode: '+212' },
    { code: 'DZ', name: 'Algérie', dialCode: '+213' },
    { code: 'TN', name: 'Tunisie', dialCode: '+216' }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private businessService: BusinessService,
    private bookingService: BookingService
  ) {
    this.customerForm = this.fb.group({
      firstName: ['', [Validators.required, Validators.maxLength(100)]],
      lastName: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email, Validators.maxLength(255)]],
      phoneCountry: ['+33', Validators.required],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^\d{6,15}$/)]],
      notes: ['', Validators.maxLength(500)]
    });
  }

  ngOnInit() {
    this.slug = this.route.snapshot.paramMap.get('slug') || '';
    if (this.slug) {
      this.loadBusinessAndServices();
    }
  }

  loadBusinessAndServices() {
    this.loading = true;
    this.error = '';

    this.businessService.getBusinessBySlug(this.slug).subscribe({
      next: (business) => {
        this.business = business;
        this.loadServices();
      },
      error: (err) => {
        this.error = err.error?.message || 'Business introuvable';
        this.loading = false;
      }
    });
  }

  loadServices() {
    this.businessService.getBusinessServices(this.slug).subscribe({
      next: (services: Service[]) => {
        this.services = services;
        this.loading = false;
      },
      error: (err: any) => {
        this.error = err.error?.message || 'Erreur lors du chargement des services';
        this.loading = false;
      }
    });
  }

  selectService(service: Service) {
    this.selectedService = service;
    this.step = 'datetime';
    this.generateCalendar();
  }

  generateCalendar() {
    const year = this.currentMonth.getFullYear();
    const month = this.currentMonth.getMonth();

    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);

    const startDayOfWeek = (firstDay.getDay() + 6) % 7; // Monday = 0
    const daysInMonth = lastDay.getDate();

    this.calendarDays = [];

    // Add empty cells for days before the month starts
    for (let i = 0; i < startDayOfWeek; i++) {
      this.calendarDays.push(null);
    }

    // Add all days of the month
    for (let day = 1; day <= daysInMonth; day++) {
      this.calendarDays.push(new Date(year, month, day));
    }
  }

  previousMonth() {
    this.currentMonth = new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth() - 1, 1);
    this.generateCalendar();
  }

  nextMonth() {
    this.currentMonth = new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth() + 1, 1);
    this.generateCalendar();
  }

  selectDate(date: Date) {
    if (!this.isDateSelectable(date)) return;

    this.selectedDate = date;
    this.selectedTimeSlot = null;
    this.loadAvailability();
  }

  isDateSelectable(date: Date): boolean {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    return date >= today;
  }

  isDateSelected(date: Date): boolean {
    if (!this.selectedDate) return false;
    return date.toDateString() === this.selectedDate.toDateString();
  }

  loadAvailability() {
    if (!this.selectedService || !this.selectedDate) return;

    this.loadingAvailability = true;
    this.availableSlots = [];

    // Format date without timezone conversion (YYYY-MM-DD)
    const year = this.selectedDate.getFullYear();
    const month = String(this.selectedDate.getMonth() + 1).padStart(2, '0');
    const day = String(this.selectedDate.getDate()).padStart(2, '0');
    const dateStr = `${year}-${month}-${day}`;

    this.bookingService.getAvailability(this.slug, this.selectedService.id, dateStr).subscribe({
      next: (response: AvailabilityResponse) => {
        this.availableSlots = response.availableSlots.filter(slot => slot.available);
        this.loadingAvailability = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors du chargement des disponibilités';
        this.loadingAvailability = false;
      }
    });
  }

  selectTimeSlot(slot: TimeSlot) {
    this.selectedTimeSlot = slot;
    this.step = 'customer';
  }

  onSubmit() {
    if (this.customerForm.invalid || !this.selectedService || !this.selectedDate || !this.selectedTimeSlot) {
      return;
    }

    this.loading = true;
    this.error = '';

    // Create appointment datetime without timezone issues
    const year = this.selectedDate.getFullYear();
    const month = this.selectedDate.getMonth();
    const day = this.selectedDate.getDate();
    const [hours, minutes] = this.selectedTimeSlot.startTime.split(':');
    const appointmentDateTime = new Date(year, month, day, parseInt(hours), parseInt(minutes), 0);

    const formValue = this.customerForm.value;
    const phone = formValue.phoneCountry + formValue.phoneNumber;

    const request: AppointmentRequest = {
      serviceId: this.selectedService.id,
      appointmentDatetime: appointmentDateTime.toISOString(),
      customer: {
        firstName: formValue.firstName,
        lastName: formValue.lastName,
        email: formValue.email,
        phone: phone
      },
      notes: formValue.notes || undefined
    };

    this.bookingService.createBooking(this.slug, request).subscribe({
      next: () => {
        this.step = 'confirmation';
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de la réservation';
        this.loading = false;
      }
    });
  }

  back() {
    if (this.step === 'datetime') {
      this.step = 'service';
      this.selectedService = null;
    } else if (this.step === 'customer') {
      this.step = 'datetime';
      this.selectedTimeSlot = null;
    }
  }

  formatDate(date: Date): string {
    return date.toLocaleDateString('fr-FR', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' });
  }

  formatTime(time: string): string {
    return time.substring(0, 5);
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'EUR' }).format(price);
  }

  getMonthYear(): string {
    return this.currentMonth.toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' });
  }
}
