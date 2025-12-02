import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ServiceService } from '../../services/service.service';
import { Service, ServiceRequest } from '../../models/business.model';

@Component({
  selector: 'app-services-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './services-management.component.html',
  styleUrl: './services-management.component.scss'
})
export class ServicesManagementComponent implements OnInit {
  services: Service[] = [];
  serviceForm: FormGroup;
  loading = false;
  saving = false;
  error = '';
  success = '';

  editingService: Service | null = null;
  showForm = false;

  colors = [
    { value: '#3B82F6', label: 'Bleu' },
    { value: '#10B981', label: 'Vert' },
    { value: '#F59E0B', label: 'Orange' },
    { value: '#EF4444', label: 'Rouge' },
    { value: '#8B5CF6', label: 'Violet' },
    { value: '#EC4899', label: 'Rose' },
    { value: '#14B8A6', label: 'Turquoise' },
    { value: '#F97316', label: 'Orange foncé' }
  ];

  constructor(
    private fb: FormBuilder,
    private serviceService: ServiceService
  ) {
    this.serviceForm = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', Validators.maxLength(500)],
      durationMinutes: [30, [Validators.required, Validators.min(5), Validators.max(480)]],
      price: [0, [Validators.required, Validators.min(0)]],
      color: ['#3B82F6', Validators.required],
      isActive: [true]
    });
  }

  ngOnInit() {
    this.loadServices();
  }

  loadServices() {
    this.loading = true;
    this.error = '';

    this.serviceService.getServices().subscribe({
      next: (services: Service[]) => {
        this.services = services.sort((a: Service, b: Service) => a.displayOrder - b.displayOrder);
        this.loading = false;
      },
      error: (err: any) => {
        this.error = err.error?.message || 'Erreur lors du chargement des services';
        this.loading = false;
      }
    });
  }

  openCreateForm() {
    this.editingService = null;
    this.serviceForm.reset({
      name: '',
      description: '',
      durationMinutes: 30,
      price: 0,
      color: '#3B82F6',
      isActive: true
    });
    this.showForm = true;
  }

  openEditForm(service: Service) {
    this.editingService = service;
    this.serviceForm.patchValue({
      name: service.name,
      description: service.description || '',
      durationMinutes: service.durationMinutes,
      price: service.price,
      color: service.color,
      isActive: service.isActive
    });
    this.showForm = true;
  }

  cancelForm() {
    this.showForm = false;
    this.editingService = null;
    this.serviceForm.reset();
  }

  onSubmit() {
    if (this.serviceForm.invalid) {
      return;
    }

    this.saving = true;
    this.error = '';
    this.success = '';

    const request: ServiceRequest = this.serviceForm.value;

    const operation = this.editingService
      ? this.serviceService.updateService(this.editingService.id, request)
      : this.serviceService.createService(request);

    operation.subscribe({
      next: () => {
        this.success = this.editingService
          ? 'Service mis à jour avec succès !'
          : 'Service créé avec succès !';
        this.saving = false;
        this.showForm = false;
        this.editingService = null;
        this.loadServices();
        setTimeout(() => this.success = '', 3000);
      },
      error: (err: any) => {
        this.error = err.error?.message || 'Erreur lors de l\'enregistrement du service';
        this.saving = false;
      }
    });
  }

  deleteService(service: Service) {
    if (!confirm(`Êtes-vous sûr de vouloir supprimer le service "${service.name}" ?`)) {
      return;
    }

    this.serviceService.deleteService(service.id).subscribe({
      next: () => {
        this.success = 'Service supprimé avec succès !';
        this.loadServices();
        setTimeout(() => this.success = '', 3000);
      },
      error: (err: any) => {
        this.error = err.error?.message || 'Erreur lors de la suppression du service';
      }
    });
  }

  formatDuration(minutes: number): string {
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    if (hours > 0 && mins > 0) {
      return `${hours}h${mins}`;
    } else if (hours > 0) {
      return `${hours}h`;
    } else {
      return `${mins}min`;
    }
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('fr-FR', {
      style: 'currency',
      currency: 'EUR'
    }).format(price);
  }
}
