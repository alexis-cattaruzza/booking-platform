import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../../services/api.service';

@Component({
  selector: 'app-business-reset-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './business-reset-password.component.html',
  styleUrl: './business-reset-password.component.scss'
})
export class BusinessResetPasswordComponent {
  passwordForm: FormGroup;
  isSubmitting = false;
  error = '';
  success = '';
  showCurrentPassword = false;
  showNewPassword = false;
  showConfirmPassword = false;

  constructor(
    private fb: FormBuilder,
    private api: ApiService,
    private router: Router
  ) {
    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).*$/)
      ]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword');
    const confirmPassword = form.get('confirmPassword');

    if (newPassword && confirmPassword && newPassword.value !== confirmPassword.value) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }

    return null;
  }

  goBack(): void {
    this.router.navigate(['/business/dashboard']);
  }

  onSubmit(): void {
    if (this.passwordForm.invalid) {
      return;
    }

    this.isSubmitting = true;
    this.error = '';
    this.success = '';

    const { currentPassword, newPassword } = this.passwordForm.value;

    this.api.post('/businesses/change-password', {
      currentPassword,
      newPassword
    }).subscribe({
      next: (response: any) => {
        this.isSubmitting = false;
        this.success = 'Mot de passe modifié avec succès !';
        this.passwordForm.reset();

        // Redirect after 2 seconds
        setTimeout(() => {
          this.router.navigate(['/business/dashboard']);
        }, 2000);
      },
      error: (err) => {
        console.error('Error changing password:', err);
        this.error = err.error?.message || 'Erreur lors de la modification du mot de passe';
        this.isSubmitting = false;
      }
    });
  }

  toggleCurrentPasswordVisibility(): void {
    this.showCurrentPassword = !this.showCurrentPassword;
  }

  toggleNewPasswordVisibility(): void {
    this.showNewPassword = !this.showNewPassword;
  }

  toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword = !this.showConfirmPassword;
  }

  get currentPassword() {
    return this.passwordForm.get('currentPassword');
  }

  get newPassword() {
    return this.passwordForm.get('newPassword');
  }

  get confirmPassword() {
    return this.passwordForm.get('confirmPassword');
  }

  // Password validation helpers
  hasMinLength(): boolean {
    return this.newPassword?.value?.length >= 8;
  }

  hasUppercase(): boolean {
    return /[A-Z]/.test(this.newPassword?.value || '');
  }

  hasLowercase(): boolean {
    return /[a-z]/.test(this.newPassword?.value || '');
  }

  hasDigit(): boolean {
    return /\d/.test(this.newPassword?.value || '');
  }
}
