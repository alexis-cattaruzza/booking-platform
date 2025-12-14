import { Component, Input, OnInit, OnDestroy, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule, registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { ApiService } from '../../services/api.service';
import { interval, Subscription } from 'rxjs';

// Register French locale
registerLocaleData(localeFr);

@Component({
  selector: 'app-deletion-banner',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './deletion-banner.component.html',
  styleUrl: './deletion-banner.component.scss'
})
export class DeletionBannerComponent implements OnInit, OnDestroy, OnChanges {
  @Input() deletedAt: string | null = null;

  daysRemaining = 0;
  deletionDate: Date | null = null;
  cancelling = false;
  error = '';
  success = '';

  private timerSubscription?: Subscription;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    console.log('DeletionBannerComponent ngOnInit - deletedAt:', this.deletedAt);
    if (this.deletedAt) {
      this.calculateDaysRemaining();
      // Update countdown every hour
      this.timerSubscription = interval(3600000).subscribe(() => {
        this.calculateDaysRemaining();
      });
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    console.log('DeletionBannerComponent ngOnChanges:', changes);
    if (changes['deletedAt'] && changes['deletedAt'].currentValue) {
      this.calculateDaysRemaining();
    }
  }

  ngOnDestroy() {
    this.timerSubscription?.unsubscribe();
  }

  private calculateDaysRemaining() {
    if (!this.deletedAt) {
      console.log('No deletedAt value');
      return;
    }

    console.log('Raw deletedAt input:', this.deletedAt, 'Type:', typeof this.deletedAt);

    // Parse the deletedAt timestamp
    const deletedDate = new Date(this.deletedAt);

    // Check if date is valid
    if (isNaN(deletedDate.getTime())) {
      console.error('Invalid date format:', this.deletedAt);
      this.daysRemaining = 0;
      return;
    }

    // Set time to start of day for deletedDate to avoid time-of-day issues
    deletedDate.setHours(0, 0, 0, 0);

    // Calculate the effective deletion date (30 days after deletion request)
    this.deletionDate = new Date(deletedDate);
    this.deletionDate.setDate(this.deletionDate.getDate() + 30);

    // Calculate days remaining from now
    const now = new Date();
    now.setHours(0, 0, 0, 0); // Start of today

    const diffMs = this.deletionDate.getTime() - now.getTime();
    const diffDays = diffMs / (1000 * 60 * 60 * 24);

    // Round up to show full days remaining
    this.daysRemaining = Math.max(0, Math.ceil(diffDays));
  }

  cancelDeletion() {
    this.cancelling = true;
    this.error = '';
    this.success = '';

    this.apiService.post('/businesses/cancel-deletion', {}).subscribe({
      next: () => {
        this.success = 'Suppression annulée avec succès ! Votre compte est maintenant actif.';
        this.cancelling = false;

        // Reload page after 2 seconds to refresh business data
        setTimeout(() => {
          window.location.reload();
        }, 2000);
      },
      error: (err) => {
        this.error = err.error?.message || 'Erreur lors de l\'annulation de la suppression';
        this.cancelling = false;
      }
    });
  }
}
