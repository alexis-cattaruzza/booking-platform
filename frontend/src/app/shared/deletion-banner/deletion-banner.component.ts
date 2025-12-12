import { Component, Input, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ApiService } from '../../services/api.service';
import { interval, Subscription } from 'rxjs';

@Component({
  selector: 'app-deletion-banner',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './deletion-banner.component.html',
  styleUrl: './deletion-banner.component.scss'
})
export class DeletionBannerComponent implements OnInit, OnDestroy {
  @Input() deletedAt: string | null = null;

  daysRemaining = 0;
  deletionDate: Date | null = null;
  cancelling = false;
  error = '';
  success = '';

  private timerSubscription?: Subscription;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    if (this.deletedAt) {
      this.calculateDaysRemaining();
      // Update countdown every hour
      this.timerSubscription = interval(3600000).subscribe(() => {
        this.calculateDaysRemaining();
      });
    }
  }

  ngOnDestroy() {
    this.timerSubscription?.unsubscribe();
  }

  private calculateDaysRemaining() {
    if (!this.deletedAt) return;

    const deletedDate = new Date(this.deletedAt);
    this.deletionDate = new Date(deletedDate);
    this.deletionDate.setDate(this.deletionDate.getDate() + 30);

    const now = new Date();
    const diff = this.deletionDate.getTime() - now.getTime();
    this.daysRemaining = Math.max(0, Math.ceil(diff / (1000 * 60 * 60 * 24)));
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
