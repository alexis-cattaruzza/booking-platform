import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { BusinessService } from '../../services/business.service';
import { Business, UpdateBusinessRequest } from '../../models/business.model';

interface CategoryOption {
  value: string;
  label: string;
}

@Component({
  selector: 'app-business-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './business-profile.component.html',
  styleUrl: './business-profile.component.scss'
})
export class BusinessProfileComponent implements OnInit {
  profileForm: FormGroup;
  business: Business | null = null;
  loading = false;
  saving = false;
  error = '';
  success = '';

  categories: CategoryOption[] = [
    { value: 'HAIRDRESSER', label: 'Coiffure' },
    { value: 'BEAUTY', label: 'Beauté & Esthétique' },
    { value: 'HEALTH', label: 'Santé & Bien-être' },
    { value: 'SPORT', label: 'Sport & Fitness' },
    { value: 'GARAGE', label: 'Garage & Automobile' },
    { value: 'OTHER', label: 'Autre' }
  ];

  constructor(
    private fb: FormBuilder,
    private businessService: BusinessService
  ) {
    this.profileForm = this.fb.group({
      businessName: ['', [Validators.required, Validators.maxLength(255)]],
      description: ['', Validators.maxLength(1000)],
      category: [''],
      address: ['', Validators.maxLength(500)],
      city: ['', Validators.maxLength(100)],
      postalCode: ['', Validators.maxLength(10)],
      phone: ['', Validators.maxLength(20)],
      email: ['', [Validators.email, Validators.maxLength(255)]],
      logoUrl: ['']
    });
  }

  ngOnInit() {
    this.loadBusiness();
  }

  loadBusiness() {
    this.loading = true;
    this.error = '';

    this.businessService.getMyBusiness().subscribe({
      next: (business) => {
        this.business = business;
        this.profileForm.patchValue({
          businessName: business.businessName,
          description: business.description || '',
          category: business.category || '',
          address: business.address || '',
          city: business.city || '',
          postalCode: business.postalCode || '',
          phone: business.phone || '',
          email: business.email || '',
          logoUrl: business.logoUrl || ''
        });
        this.loading = false;
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors du chargement du profil';
        this.loading = false;
      }
    });
  }

  onSubmit() {
    if (this.profileForm.invalid) {
      return;
    }

    this.saving = true;
    this.error = '';
    this.success = '';

    const request: UpdateBusinessRequest = this.profileForm.value;

    this.businessService.updateMyBusiness(request).subscribe({
      next: (business) => {
        this.business = business;
        this.success = 'Profil mis à jour avec succès !';
        this.saving = false;
        setTimeout(() => this.success = '', 5000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de la mise à jour du profil';
        this.saving = false;
      }
    });
  }
}
