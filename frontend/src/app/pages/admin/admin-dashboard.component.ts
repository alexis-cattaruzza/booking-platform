import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminService } from '../../services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="dashboard-page">
      <div class="header">
        <h1>Tableau de bord administrateur</h1>
        <p>Vue d'ensemble de la plateforme</p>
      </div>

      <div class="recent-activity">
        <h2>Activité Récente</h2>
        <div class="activity-list" *ngIf="recentLogs.length > 0">
          <div class="activity-item" *ngFor="let log of recentLogs">
            <div class="activity-icon" [class.success]="log.status === 'SUCCESS'" [class.failure]="log.status === 'FAILURE'">
              {{ log.status === 'SUCCESS' ? '✓' : '✗' }}
            </div>
            <div class="activity-content">
              <div class="activity-header">
                <span class="activity-user">{{ log.username }}</span>
                <span class="activity-action">{{ log.action }}</span>
              </div>
              <div class="activity-meta">
                <span>{{ log.ipAddress }}</span>
                <span>{{ formatDate(log.createdAt) }}</span>
              </div>
            </div>
          </div>
        </div>
        <div *ngIf="recentLogs.length === 0" class="no-data">
          Aucune activité récente
        </div>
      </div>

      <div class="quick-actions">
        <h2>Actions Rapides</h2>
        <div class="actions-grid">
          <button class="action-btn" routerLink="/admin/audit-logs">
            <span class="action-icon">📋</span>
            <span>Tous les Logs</span>
          </button>
          <button class="action-btn" routerLink="/admin/businesses">
            <span class="action-icon">🏢</span>
            <span>Businesses</span>
          </button>
          <button class="action-btn" routerLink="/admin/audit-logs?status=FAILURE">
            <span class="action-icon">⚠️</span>
            <span>Échecs</span>
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .dashboard-page {
      width: 100%;
    }

    .header {
      margin-bottom: 2rem;
      padding-bottom: 1rem;
      border-bottom: 2px solid #e5e7eb;
    }

    .header h1 {
      font-size: 2rem;
      font-weight: 700;
      color: #1a1a1a;
      margin-bottom: 0.5rem;
    }

    .header p {
      color: #666;
      font-size: 1rem;
    }

    .recent-activity, .quick-actions {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      margin-bottom: 2rem;
    }

    .recent-activity h2, .quick-actions h2 {
      font-size: 1.25rem;
      font-weight: 600;
      margin-bottom: 1rem;
      color: #1a1a1a;
    }

    .activity-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .activity-item {
      display: flex;
      gap: 1rem;
      padding: 1rem;
      border-radius: 8px;
      background: #f8f9fa;
    }

    .activity-icon {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      flex-shrink: 0;
    }

    .activity-icon.success {
      background: #d4edda;
      color: #155724;
    }

    .activity-icon.failure {
      background: #f8d7da;
      color: #721c24;
    }

    .activity-content {
      flex: 1;
    }

    .activity-header {
      display: flex;
      gap: 1rem;
      margin-bottom: 0.25rem;
    }

    .activity-user {
      font-weight: 600;
      color: #1a1a1a;
    }

    .activity-action {
      color: #666;
      font-size: 0.875rem;
    }

    .activity-meta {
      display: flex;
      gap: 1rem;
      font-size: 0.75rem;
      color: #999;
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 1rem;
    }

    .action-btn {
      background: #f8f9fa;
      border: 2px solid #dee2e6;
      border-radius: 8px;
      padding: 1rem;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
      cursor: pointer;
      transition: all 0.2s;
    }

    .action-btn:hover {
      background: #e9ecef;
      border-color: #007bff;
    }

    .action-icon {
      font-size: 2rem;
    }

    .no-data {
      text-align: center;
      padding: 2rem;
      color: #999;
    }
  `]
})
export class AdminDashboardComponent implements OnInit {
  recentLogs: any[] = [];

  constructor(
    private adminService: AdminService
  ) {}

  ngOnInit() {
    this.loadRecentActivity();
  }

  loadRecentActivity() {
    this.adminService.getAuditLogs(0, 10).subscribe(data => {
      this.recentLogs = data.content;
    });
  }

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'À l\'instant';
    if (minutes < 60) return `Il y a ${minutes}min`;
    if (hours < 24) return `Il y a ${hours}h`;
    if (days < 7) return `Il y a ${days}j`;
    return date.toLocaleDateString('fr-FR');
  }
}
