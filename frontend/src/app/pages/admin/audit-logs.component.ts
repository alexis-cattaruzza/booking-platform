import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { AdminService, AuditLog, AuditLogPage } from '../../services/admin.service';

@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="audit-logs">
      <div class="header">
        <div class="title-section">
          <h1>Audit Logs</h1>
          <p>{{ totalElements }} événements enregistrés</p>
        </div>
      </div>

      <div class="filters-card">
        <h3>Filtres</h3>
        <div class="filters-grid">
          <div class="filter-group">
            <label>Action</label>
            <select [(ngModel)]="selectedAction" (change)="applyFilters()">
              <option value="">Toutes les actions</option>
              <option value="LOGIN">LOGIN</option>
              <option value="LOGIN_FAILED">LOGIN_FAILED</option>
              <option value="LOGOUT">LOGOUT</option>
              <option value="REGISTER">REGISTER</option>
              <option value="PASSWORD_RESET_REQUESTED">PASSWORD_RESET_REQUESTED</option>
              <option value="PASSWORD_RESET_COMPLETED">PASSWORD_RESET_COMPLETED</option>
              <option value="UNAUTHORIZED_ACCESS">UNAUTHORIZED_ACCESS</option>
            </select>
          </div>

          <div class="filter-group">
            <label>Status</label>
            <select [(ngModel)]="selectedStatus" (change)="applyFilters()">
              <option value="">Tous les status</option>
              <option value="SUCCESS">SUCCESS</option>
              <option value="FAILURE">FAILURE</option>
              <option value="ERROR">ERROR</option>
            </select>
          </div>

          <div class="filter-group">
            <label>Recherche (email/IP)</label>
            <input
              type="text"
              [(ngModel)]="searchTerm"
              (ngModelChange)="onSearchChange()"
              placeholder="Rechercher..."
            />
          </div>

          <div class="filter-actions">
            <button class="btn-reset" (click)="resetFilters()">
              Réinitialiser
            </button>
          </div>
        </div>
      </div>

      <div class="table-card">
        <div class="table-container">
          <table class="audit-table">
            <thead>
              <tr>
                <th>Status</th>
                <th>Utilisateur</th>
                <th>Action</th>
                <th>IP Address</th>
                <th>Date/Heure</th>
                <th>Détails</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let log of logs" [class.failure]="log.status === 'FAILURE'">
                <td>
                  <span class="status-badge" [class]="log.status.toLowerCase()">
                    {{ log.status }}
                  </span>
                </td>
                <td>
                  <div class="user-cell">
                    <strong>{{ log.username }}</strong>
                  </div>
                </td>
                <td>
                  <span class="action-badge">{{ formatAction(log.action) }}</span>
                </td>
                <td>
                  <code class="ip-address">{{ log.ipAddress }}</code>
                </td>
                <td>
                  <div class="date-cell">
                    <div>{{ formatDate(log.createdAt) }}</div>
                    <small>{{ formatTime(log.createdAt) }}</small>
                  </div>
                </td>
                <td>
                  <button class="btn-details" (click)="showDetails(log)">
                    Voir
                  </button>
                </td>
              </tr>
            </tbody>
          </table>

          <div *ngIf="logs.length === 0" class="no-data">
            Aucun log trouvé pour ces critères
          </div>
        </div>

        <div class="pagination" *ngIf="totalPages > 1">
          <button
            class="btn-page"
            [disabled]="currentPage === 0"
            (click)="goToPage(currentPage - 1)">
            ← Précédent
          </button>
          <span class="page-info">
            Page {{ currentPage + 1 }} sur {{ totalPages }}
          </span>
          <button
            class="btn-page"
            [disabled]="currentPage >= totalPages - 1"
            (click)="goToPage(currentPage + 1)">
            Suivant →
          </button>
        </div>
      </div>

      <!-- Modal Détails -->
      <div class="modal" *ngIf="selectedLog" (click)="closeDetails()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Détails du Log</h2>
            <button class="btn-close" (click)="closeDetails()">✕</button>
          </div>
          <div class="modal-body">
            <div class="detail-row">
              <span class="detail-label">ID:</span>
              <span>{{ selectedLog.id }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Utilisateur:</span>
              <span>{{ selectedLog.username }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Action:</span>
              <span class="action-badge">{{ selectedLog.action }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">Status:</span>
              <span class="status-badge" [class]="selectedLog.status.toLowerCase()">
                {{ selectedLog.status }}
              </span>
            </div>
            <div class="detail-row">
              <span class="detail-label">IP Address:</span>
              <code>{{ selectedLog.ipAddress }}</code>
            </div>
            <div class="detail-row">
              <span class="detail-label">User Agent:</span>
              <code class="user-agent">{{ selectedLog.userAgent }}</code>
            </div>
            <div class="detail-row">
              <span class="detail-label">Date/Heure:</span>
              <span>{{ formatDateTime(selectedLog.createdAt) }}</span>
            </div>
            <div class="detail-row" *ngIf="selectedLog.errorMessage">
              <span class="detail-label">Erreur:</span>
              <span class="error-message">{{ selectedLog.errorMessage }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .audit-logs {
      padding: 2rem;
      max-width: 1600px;
      margin: 0 auto;
    }

    .header {
      margin-bottom: 2rem;
    }

    .title-section {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .back-link {
      color: #007bff;
      text-decoration: none;
      font-weight: 500;
    }

    .back-link:hover {
      text-decoration: underline;
    }

    h1 {
      font-size: 2rem;
      font-weight: 700;
      color: #1a1a1a;
      margin: 0;
    }

    .filters-card, .table-card {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      margin-bottom: 1.5rem;
    }

    .filters-card h3 {
      margin-bottom: 1rem;
      color: #1a1a1a;
    }

    .filters-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
      align-items: end;
    }

    .filter-group {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }

    .filter-group label {
      font-weight: 500;
      font-size: 0.875rem;
      color: #666;
    }

    .filter-group select,
    .filter-group input {
      padding: 0.5rem;
      border: 2px solid #dee2e6;
      border-radius: 6px;
      font-size: 0.875rem;
    }

    .filter-group select:focus,
    .filter-group input:focus {
      outline: none;
      border-color: #007bff;
    }

    .btn-reset {
      padding: 0.5rem 1rem;
      background: #f8f9fa;
      border: 2px solid #dee2e6;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 500;
      transition: all 0.2s;
    }

    .btn-reset:hover {
      background: #e9ecef;
      border-color: #adb5bd;
    }

    .table-container {
      overflow-x: auto;
    }

    .audit-table {
      width: 100%;
      border-collapse: collapse;
    }

    .audit-table th {
      background: #f8f9fa;
      padding: 0.75rem;
      text-align: left;
      font-weight: 600;
      font-size: 0.875rem;
      color: #495057;
      border-bottom: 2px solid #dee2e6;
    }

    .audit-table td {
      padding: 1rem 0.75rem;
      border-bottom: 1px solid #dee2e6;
    }

    .audit-table tr:hover {
      background: #f8f9fa;
    }

    .audit-table tr.failure {
      background: #fff5f5;
    }

    .status-badge {
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
    }

    .status-badge.success {
      background: #d4edda;
      color: #155724;
    }

    .status-badge.failure {
      background: #f8d7da;
      color: #721c24;
    }

    .status-badge.error {
      background: #fff3cd;
      color: #856404;
    }

    .action-badge {
      background: #e7f3ff;
      color: #0066cc;
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      font-size: 0.75rem;
      font-weight: 500;
    }

    .ip-address {
      background: #f8f9fa;
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
      font-size: 0.875rem;
      font-family: monospace;
    }

    .date-cell {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .date-cell small {
      color: #999;
      font-size: 0.75rem;
    }

    .btn-details {
      padding: 0.25rem 0.75rem;
      background: #007bff;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.875rem;
    }

    .btn-details:hover {
      background: #0056b3;
    }

    .pagination {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 1rem;
      margin-top: 1.5rem;
      padding-top: 1.5rem;
      border-top: 1px solid #dee2e6;
    }

    .btn-page {
      padding: 0.5rem 1rem;
      background: white;
      border: 2px solid #dee2e6;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 500;
    }

    .btn-page:hover:not(:disabled) {
      border-color: #007bff;
      color: #007bff;
    }

    .btn-page:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .page-info {
      font-weight: 500;
    }

    .modal {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }

    .modal-content {
      background: white;
      border-radius: 12px;
      width: 90%;
      max-width: 600px;
      max-height: 80vh;
      overflow: auto;
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.5rem;
      border-bottom: 1px solid #dee2e6;
    }

    .modal-header h2 {
      margin: 0;
      font-size: 1.25rem;
    }

    .btn-close {
      background: none;
      border: none;
      font-size: 1.5rem;
      cursor: pointer;
      color: #999;
    }

    .btn-close:hover {
      color: #666;
    }

    .modal-body {
      padding: 1.5rem;
    }

    .detail-row {
      display: flex;
      gap: 1rem;
      padding: 0.75rem 0;
      border-bottom: 1px solid #f8f9fa;
    }

    .detail-label {
      font-weight: 600;
      min-width: 120px;
      color: #666;
    }

    .user-agent {
      font-size: 0.75rem;
      word-break: break-all;
    }

    .error-message {
      color: #dc3545;
      font-weight: 500;
    }

    .no-data {
      text-align: center;
      padding: 3rem;
      color: #999;
    }
  `]
})
export class AuditLogsComponent implements OnInit {
  logs: AuditLog[] = [];
  totalElements = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 20;

  selectedAction = '';
  selectedStatus = '';
  searchTerm = '';
  selectedLog: AuditLog | null = null;

  private searchTimeout: any;

  constructor(
    private adminService: AdminService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    // Charger les paramètres de l'URL
    this.route.queryParams.subscribe(params => {
      this.selectedAction = params['action'] || '';
      this.selectedStatus = params['status'] || '';
      this.loadLogs();
    });
  }

  loadLogs() {
    let observable;

    if (this.selectedAction) {
      observable = this.adminService.getAuditLogsByAction(this.selectedAction, this.currentPage, this.pageSize);
    } else if (this.selectedStatus) {
      observable = this.adminService.getAuditLogsByStatus(this.selectedStatus, this.currentPage, this.pageSize);
    } else {
      observable = this.adminService.getAuditLogs(this.currentPage, this.pageSize);
    }

    observable.subscribe((data: AuditLogPage) => {
      this.logs = this.filterBySearch(data.content);
      this.totalElements = data.totalElements;
      this.totalPages = data.totalPages;
    });
  }

  filterBySearch(logs: AuditLog[]): AuditLog[] {
    if (!this.searchTerm) return logs;

    const term = this.searchTerm.toLowerCase();
    return logs.filter(log =>
      log.username.toLowerCase().includes(term) ||
      log.ipAddress.toLowerCase().includes(term)
    );
  }

  applyFilters() {
    this.currentPage = 0;
    this.loadLogs();
  }

  onSearchChange() {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => {
      this.applyFilters();
    }, 300);
  }

  resetFilters() {
    this.selectedAction = '';
    this.selectedStatus = '';
    this.searchTerm = '';
    this.currentPage = 0;
    this.loadLogs();
  }

  goToPage(page: number) {
    this.currentPage = page;
    this.loadLogs();
  }

  showDetails(log: AuditLog) {
    this.selectedLog = log;
  }

  closeDetails() {
    this.selectedLog = null;
  }

  formatAction(action: string): string {
    return action.replace(/_/g, ' ');
  }

  formatDate(dateString: string): string {
    return new Date(dateString).toLocaleDateString('fr-FR');
  }

  formatTime(dateString: string): string {
    return new Date(dateString).toLocaleTimeString('fr-FR');
  }

  formatDateTime(dateString: string): string {
    return new Date(dateString).toLocaleString('fr-FR');
  }
}
