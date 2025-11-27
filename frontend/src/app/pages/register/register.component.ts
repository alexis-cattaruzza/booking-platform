import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';

interface Country {
  code: string;
  name: string;
  dialCode: string;
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  registerForm: FormGroup;
  loading = false;
  error = '';

  countries: Country[] = [
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
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      phoneCountry: ['+33', Validators.required],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^\d{6,15}$/)]],
      businessName: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      return;
    }

    this.loading = true;
    this.error = '';

    const formValue = {
      ...this.registerForm.value,
      phone: this.registerForm.value.phoneCountry + this.registerForm.value.phoneNumber
    };

    // Remove phoneCountry and phoneNumber from the object
    delete formValue.phoneCountry;
    delete formValue.phoneNumber;

    this.authService.register(formValue).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.error = err.error?.message || 'Registration failed. Please try again.';
        this.loading = false;
      }
    });
  }
}
