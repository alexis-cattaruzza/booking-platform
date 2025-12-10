import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminService, Business } from '../../services/admin.service';

@Component({
  selector: 'app-business-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  template: `
    <div class="business-management">
      <div class="header">
        <div class="title-section">
          <a routerLink="/admin" class="back-link">← Dashboard</a>
          <h1>Gestion des Businesses</h1>
          <p>{{ totalBusinesses }} entreprises enregistrées</p>
        </div>
      </div>

      <div class="filters-card">
        <h3>Filtres et Recherche</h3>
        <div class="filters-grid">
          <div class="filter-group">
            <label>Statut</label>
            <select [(ngModel)]="selectedStatus" (change)="applyFilters()">
              <option value="">Tous les statuts</option>
              <option value="ACTIVE">Actif</option>
              <option value="SUSPENDED">Suspendu</option>
              <option value="DELETED">Supprimé</option>
            </select>
          </div>

          <div class="filter-group">
            <label>Recherche</label>
            <input
              type="text"
              [(ngModel)]="searchTerm"
              (ngModelChange)="onSearchChange()"
              placeholder="Nom, email, ville..."
            />
          </div>

          <div class="filter-actions">
            <button class="btn-reset" (click)="resetFilters()">
              🔄 Réinitialiser
            </button>
          </div>
        </div>
      </div>

      <div class="table-card">
        <div class="table-container">
          <table class="business-table">
            <thead>
              <tr>
                <th>Business</th>
                <th>Propriétaire</th>
                <th>Contact</th>
                <th>Ville</th>
                <th>Créé le</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let business of businesses" [class.suspended]="business.status === 'SUSPENDED'">
                <td>
                  <div class="business-cell">
                    <strong>{{ business.businessName }}</strong>
                    <small>{{ business.slug }}</small>
                  </div>
                </td>
                <td>
                  <div class="owner-cell">
                    <strong>{{ business.ownerFirstName }} {{ business.ownerLastName }}</strong>
                    <small>{{ business.email }}</small>
                  </div>
                </td>
                <td>
                  <div class="contact-cell">
                    <div>📧 {{ business.email }}</div>
                    <div *ngIf="business.phone">📞 {{ business.phone }}</div>
                  </div>
                </td>
                <td>
                  <div class="location-cell">
                    <div>{{ business.city }}</div>
                    <small *ngIf="business.postalCode">{{ business.postalCode }}</small>
                  </div>
                </td>
                <td>
                  <div class="date-cell">
                    <div>{{ formatDate(business.createdAt) }}</div>
                    <small>{{ formatTime(business.createdAt) }}</small>
                  </div>
                </td>
                <td>
                  <span class="status-badge" [class]="business.status.toLowerCase()">
                    {{ business.status }}
                  </span>
                </td>
                <td>
                  <div class="actions-cell">
                    <button class="btn-action btn-view" (click)="viewBusiness(business)" title="Voir les détails">
                      👁️
                    </button>
                    <button
                      *ngIf="business.status === 'ACTIVE'"
                      class="btn-action btn-suspend"
                      (click)="suspendBusiness(business)"
                      title="Suspendre">
                      ⏸️
                    </button>
                    <button
                      *ngIf="business.status === 'SUSPENDED'"
                      class="btn-action btn-activate"
                      (click)="activateBusiness(business)"
                      title="Activer">
                      ▶️
                    </button>
                    <button
                      class="btn-action btn-delete"
                      (click)="deleteBusiness(business)"
                      title="Supprimer">
                      🗑️
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>

          <div *ngIf="businesses.length === 0" class="no-data">
            Aucun business trouvé pour ces critères
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

      <!-- Modal Détails Business -->
      <div class="modal" *ngIf="selectedBusiness" (click)="closeDetails()">
        <div class="modal-content" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Détails du Business</h2>
            <button class="btn-close" (click)="closeDetails()">✕</button>
          </div>
          <div class="modal-body">
            <div class="detail-section">
              <h3>Informations générales</h3>
              <div class="detail-row">
                <span class="detail-label">ID:</span>
                <span>{{ selectedBusiness.id }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Nom:</span>
                <span>{{ selectedBusiness.businessName }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Slug:</span>
                <span class="slug-value">{{ selectedBusiness.slug }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Description:</span>
                <span>{{ selectedBusiness.description || 'N/A' }}</span>
              </div>
            </div>

            <div class="detail-section">
              <h3>Propriétaire</h3>
              <div class="detail-row">
                <span class="detail-label">Nom:</span>
                <span>{{ selectedBusiness.ownerFirstName }} {{ selectedBusiness.ownerLastName }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Email:</span>
                <span>{{ selectedBusiness.email }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Téléphone:</span>
                <span>{{ selectedBusiness.phone || 'N/A' }}</span>
              </div>
            </div>

            <div class="detail-section">
              <h3>Adresse</h3>
              <div class="detail-row">
                <span class="detail-label">Adresse:</span>
                <span>{{ selectedBusiness.address || 'N/A' }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Ville:</span>
                <span>{{ selectedBusiness.city }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Code postal:</span>
                <span>{{ selectedBusiness.postalCode || 'N/A' }}</span>
              </div>
            </div>

            <div class="detail-section">
              <h3>Métadonnées</h3>
              <div class="detail-row">
                <span class="detail-label">Statut:</span>
                <span class="status-badge" [class]="selectedBusiness.status.toLowerCase()">
                  {{ selectedBusiness.status }}
                </span>
              </div>
              <div class="detail-row">
                <span class="detail-label">Créé le:</span>
                <span>{{ formatDateTime(selectedBusiness.createdAt) }}</span>
              </div>
              <div class="detail-row" *ngIf="selectedBusiness.deletedAt">
                <span class="detail-label">Supprimé le:</span>
                <span class="error-text">{{ formatDateTime(selectedBusiness.deletedAt) }}</span>
              </div>
            </div>

            <div class="modal-actions">
              <button
                *ngIf="selectedBusiness.status === 'ACTIVE'"
                class="btn-modal btn-suspend"
                (click)="suspendBusiness(selectedBusiness)">
                ⏸️ Suspendre
              </button>
              <button
                *ngIf="selectedBusiness.status === 'SUSPENDED'"
                class="btn-modal btn-activate"
                (click)="activateBusiness(selectedBusiness)">
                ▶️ Activer
              </button>
              <button
                class="btn-modal btn-delete"
                (click)="deleteBusiness(selectedBusiness)">
                🗑️ Supprimer
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Modal de Confirmation -->
      <div class="modal" *ngIf="confirmAction" (click)="cancelAction()">
        <div class="modal-content confirm-modal" (click)="$event.stopPropagation()">
          <div class="modal-header">
            <h2>Confirmation</h2>
            <button class="btn-close" (click)="cancelAction()">✕</button>
          </div>
          <div class="modal-body">
            <p class="confirm-message">{{ confirmMessage }}</p>
            <div class="confirm-actions">
              <button class="btn-modal btn-cancel" (click)="cancelAction()">
                Annuler
              </button>
              <button class="btn-modal btn-confirm" (click)="executeAction()">
                Confirmer
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .business-management {
      padding: 2rem;
      max-width: 1800px;
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

    .business-table {
      width: 100%;
      border-collapse: collapse;
    }

    .business-table th {
      background: #f8f9fa;
      padding: 0.75rem;
      text-align: left;
      font-weight: 600;
      font-size: 0.875rem;
      color: #495057;
      border-bottom: 2px solid #dee2e6;
    }

    .business-table td {
      padding: 1rem 0.75rem;
      border-bottom: 1px solid #dee2e6;
    }

    .business-table tr:hover {
      background: #f8f9fa;
    }

    .business-table tr.suspended {
      background: #fff5e6;
    }

    .business-cell {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .business-cell small {
      color: #999;
      font-size: 0.75rem;
    }

    .owner-cell {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .owner-cell small {
      color: #999;
      font-size: 0.75rem;
    }

    .contact-cell {
      font-size: 0.875rem;
    }

    .contact-cell div {
      margin: 0.25rem 0;
    }

    .location-cell {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }

    .location-cell small {
      color: #999;
      font-size: 0.75rem;
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

    .status-badge {
      padding: 0.25rem 0.75rem;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;
      display: inline-block;
    }

    .status-badge.active {
      background: #d4edda;
      color: #155724;
    }

    .status-badge.suspended {
      background: #fff3cd;
      color: #856404;
    }

    .status-badge.deleted {
      background: #f8d7da;
      color: #721c24;
    }

    .actions-cell {
      display: flex;
      gap: 0.5rem;
    }

    .btn-action {
      padding: 0.25rem 0.5rem;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 1rem;
      transition: transform 0.2s;
    }

    .btn-action:hover {
      transform: scale(1.1);
    }

    .btn-view {
      background: #007bff;
    }

    .btn-suspend {
      background: #ffc107;
    }

    .btn-activate {
      background: #28a745;
    }

    .btn-delete {
      background: #dc3545;
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
      max-width: 700px;
      max-height: 80vh;
      overflow: auto;
    }

    .confirm-modal {
      max-width: 500px;
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

    .detail-section {
      margin-bottom: 1.5rem;
      padding-bottom: 1.5rem;
      border-bottom: 1px solid #f8f9fa;
    }

    .detail-section:last-of-type {
      border-bottom: none;
    }

    .detail-section h3 {
      font-size: 1rem;
      color: #666;
      margin-bottom: 1rem;
    }

    .detail-row {
      display: flex;
      gap: 1rem;
      padding: 0.5rem 0;
    }

    .detail-label {
      font-weight: 600;
      min-width: 140px;
      color: #666;
    }

    .slug-value {
      font-family: monospace;
      background: #f8f9fa;
      padding: 0.25rem 0.5rem;
      border-radius: 4px;
    }

    .error-text {
      color: #dc3545;
      font-weight: 500;
    }

    .modal-actions {
      display: flex;
      gap: 1rem;
      margin-top: 1.5rem;
      padding-top: 1.5rem;
      border-top: 1px solid #dee2e6;
    }

    .btn-modal {
      padding: 0.5rem 1rem;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-weight: 500;
      transition: all 0.2s;
    }

    .btn-modal.btn-suspend {
      background: #ffc107;
      color: #000;
    }

    .btn-modal.btn-suspend:hover {
      background: #e0a800;
    }

    .btn-modal.btn-activate {
      background: #28a745;
      color: white;
    }

    .btn-modal.btn-activate:hover {
      background: #218838;
    }

    .btn-modal.btn-delete {
      background: #dc3545;
      color: white;
    }

    .btn-modal.btn-delete:hover {
      background: #c82333;
    }

    .confirm-message {
      font-size: 1rem;
      margin-bottom: 1.5rem;
      color: #495057;
    }

    .confirm-actions {
      display: flex;
      gap: 1rem;
      justify-content: flex-end;
    }

    .btn-cancel {
      background: #6c757d;
      color: white;
    }

    .btn-cancel:hover {
      background: #5a6268;
    }

    .btn-confirm {
      background: #dc3545;
      color: white;
    }

    .btn-confirm:hover {
      background: #c82333;
    }

    .no-data {
      text-align: center;
      padding: 3rem;
      color: #999;
    }
  `]
})
export class BusinessManagementComponent implements OnInit {
  businesses: Business[] = [];
  totalBusinesses = 0;
  totalPages = 0;
  currentPage = 0;
  pageSize = 20;

  selectedStatus = '';
  searchTerm = '';
  selectedBusiness: Business | null = null;

  confirmAction: (() => void) | null = null;
  confirmMessage = '';

  private searchTimeout: any;

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadBusinesses();
  }

  loadBusinesses() {
    this.adminService.getAllBusinesses(this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        this.businesses = this.filterBusinesses(data.content);
        this.totalBusinesses = data.totalElements;
        this.totalPages = data.totalPages;
      },
      error: (err) => {
        console.error('Error loading businesses:', err);
        // TODO: Show error notification
      }
    });
  }

  filterBusinesses(businesses: Business[]): Business[] {
    let filtered = businesses;

    if (this.selectedStatus) {
      filtered = filtered.filter(b => b.status === this.selectedStatus);
    }

    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      filtered = filtered.filter(b =>
        b.businessName.toLowerCase().includes(term) ||
        (b.email?.toLowerCase().includes(term) || false) ||
        (b.city?.toLowerCase().includes(term) || false) ||
        b.ownerFirstName.toLowerCase().includes(term) ||
        b.ownerLastName.toLowerCase().includes(term)
      );
    }

    return filtered;
  }

  applyFilters() {
    this.currentPage = 0;
    this.loadBusinesses();
  }

  onSearchChange() {
    clearTimeout(this.searchTimeout);
    this.searchTimeout = setTimeout(() => {
      this.applyFilters();
    }, 300);
  }

  resetFilters() {
    this.selectedStatus = '';
    this.searchTerm = '';
    this.currentPage = 0;
    this.loadBusinesses();
  }

  goToPage(page: number) {
    this.currentPage = page;
    this.loadBusinesses();
  }

  viewBusiness(business: Business) {
    this.selectedBusiness = business;
  }

  closeDetails() {
    this.selectedBusiness = null;
  }

  suspendBusiness(business: Business) {
    this.confirmMessage = `Êtes-vous sûr de vouloir suspendre le business "${business.businessName}" ?`;
    this.confirmAction = () => {
      this.adminService.suspendBusiness(business.id).subscribe({
        next: () => {
          console.log('Business suspended successfully');
          this.loadBusinesses();
          this.closeDetails();
          this.cancelAction();
        },
        error: (err) => {
          console.error('Error suspending business:', err);
          // TODO: Show error notification
        }
      });
    };
  }

  activateBusiness(business: Business) {
    this.confirmMessage = `Êtes-vous sûr de vouloir activer le business "${business.businessName}" ?`;
    this.confirmAction = () => {
      this.adminService.activateBusiness(business.id).subscribe({
        next: () => {
          console.log('Business activated successfully');
          this.loadBusinesses();
          this.closeDetails();
          this.cancelAction();
        },
        error: (err) => {
          console.error('Error activating business:', err);
          // TODO: Show error notification
        }
      });
    };
  }

  deleteBusiness(business: Business) {
    this.confirmMessage = `ATTENTION : Êtes-vous sûr de vouloir supprimer le business "${business.businessName}" ? Cette action est irréversible.`;
    this.confirmAction = () => {
      this.adminService.deleteBusiness(business.id).subscribe({
        next: () => {
          console.log('Business deleted successfully');
          this.loadBusinesses();
          this.closeDetails();
          this.cancelAction();
        },
        error: (err) => {
          console.error('Error deleting business:', err);
          // TODO: Show error notification
        }
      });
    };
  }

  executeAction() {
    if (this.confirmAction) {
      this.confirmAction();
    }
  }

  cancelAction() {
    this.confirmAction = null;
    this.confirmMessage = '';
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
