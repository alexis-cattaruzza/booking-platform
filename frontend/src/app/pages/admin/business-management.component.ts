import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminService, Business } from '../../services/admin.service';

@Component({
  selector: 'app-business-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './business-management.component.html',
  styleUrls: ['./business-management.component.scss']
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
