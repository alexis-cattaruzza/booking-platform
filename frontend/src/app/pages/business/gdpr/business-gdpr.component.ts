import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../../services/api.service';
import { Router } from '@angular/router';

interface GdprInfo {
  businessName: string;
  email: string;
  createdAt: string;
  deletedAt?: string | null;
  effectiveDeletionDate?: string | null;
  deletionGracePeriodDays: number;
  futureAppointmentsCount?: number;
  dataCategories: string[];
}

@Component({
  selector: 'app-business-gdpr',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './business-gdpr.component.html',
  styleUrl: './business-gdpr.component.scss'
})
export class BusinessGdprComponent implements OnInit {
  gdprInfo: GdprInfo | null = null;
  isExporting = false;
  isDeleting = false;
  showDeleteConfirm = false;
  confirmDeletion = false;
  deletionPassword = '';
  deletionReason = '';
  error = '';
  success = '';
  showPassword = false;

  constructor(
    private api: ApiService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadGdprInfo();
  }

  goBack(): void {
    this.router.navigate(['/business/dashboard']);
  }

  loadGdprInfo(): void {
    this.api.get<GdprInfo>('/businesses/gdpr/info').subscribe({
      next: (data) => this.gdprInfo = data,
      error: (err) => {
        console.error('Error loading GDPR info:', err);
        this.error = 'Erreur lors du chargement des informations';
      }
    });
  }

  exportData(): void {
    this.isExporting = true;
    this.error = '';
    this.success = '';

    this.api.getBlob('/businesses/gdpr/export').subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        const filename = `export-donnees-${this.gdprInfo?.businessName || 'business'}-${Date.now()}.json`;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        this.isExporting = false;
        this.success = 'Export réussi ! Le fichier a été téléchargé.';
      },
      error: (err) => {
        console.error('Error exporting data:', err);
        this.error = 'Erreur lors de l\'export des données';
        this.isExporting = false;
      }
    });
  }

  openDeleteConfirm(): void {
    this.showDeleteConfirm = true;
    this.error = '';
    this.success = '';
  }

  closeDeleteConfirm(): void {
    this.showDeleteConfirm = false;
    this.confirmDeletion = false;
    this.deletionPassword = '';
    this.deletionReason = '';
    this.showPassword = false;
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }

  deleteAccount(): void {
    if (!this.confirmDeletion) {
      this.error = 'Veuillez confirmer la suppression en cochant la case';
      return;
    }

    if (!this.deletionPassword || this.deletionPassword.trim() === '') {
      this.error = 'Veuillez entrer votre mot de passe pour confirmer';
      return;
    }

    this.isDeleting = true;
    this.error = '';

    this.api.post('/businesses/gdpr/delete', {
      password: this.deletionPassword,
      confirmDeletion: this.confirmDeletion,
      reason: this.deletionReason
    }).subscribe({
      next: (response: any) => {
        this.success = response.message || 'Demande de suppression enregistrée. Vous pouvez annuler cette demande pendant 30 jours.';
        this.isDeleting = false;
        this.closeDeleteConfirm();

        // Reload GDPR info to potentially show deletion status
        this.loadGdprInfo();

        // Scroll to top to see success message
        window.scrollTo({ top: 0, behavior: 'smooth' });
      },
      error: (err) => {
        console.error('Error deleting account:', err);
        this.error = err.error?.message || 'Erreur lors de la suppression du compte';
        this.isDeleting = false;
      }
    });
  }

  cancelDeletion(): void {
    if (!confirm('Êtes-vous sûr de vouloir annuler la suppression de votre compte ?')) {
      return;
    }

    this.api.post('/businesses/gdpr/cancel-deletion', {}).subscribe({
      next: (response: any) => {
        this.success = response.message || 'Votre demande de suppression a été annulée avec succès.';
        this.loadGdprInfo();
        window.scrollTo({ top: 0, behavior: 'smooth' });
      },
      error: (err) => {
        console.error('Error cancelling deletion:', err);
        this.error = err.error?.message || 'Erreur lors de l\'annulation de la suppression';
      }
    });
  }
}
