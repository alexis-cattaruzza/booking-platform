import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { NavbarLandingComponent } from '../navbar/navbar-landing.component';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarLandingComponent],
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.scss'
})
export class ContactComponent {
  formData = {
    name: '',
    email: '',
    subject: '',
    message: ''
  };

  isSubmitting = false;
  submitSuccess = false;
  submitError = '';

  onSubmit() {
    if (!this.isValid()) {
      this.submitError = 'Veuillez remplir tous les champs obligatoires';
      return;
    }

    this.isSubmitting = true;
    this.submitError = '';

    // Simulate form submission
    setTimeout(() => {
      this.submitSuccess = true;
      this.isSubmitting = false;
      this.resetForm();

      // Hide success message after 5 seconds
      setTimeout(() => {
        this.submitSuccess = false;
      }, 5000);
    }, 1000);
  }

  isValid(): boolean {
    return !!(this.formData.name && this.formData.email && this.formData.message);
  }

  resetForm() {
    this.formData = {
      name: '',
      email: '',
      subject: '',
      message: ''
    };
  }
}
