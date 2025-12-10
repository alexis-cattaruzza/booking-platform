import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AdminService } from '../../services/admin.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="admin-layout">
      <!-- Navbar -->
      <nav class="admin-navbar">
        <div class="navbar-content">
          <div class="navbar-brand">
            <span class="navbar-title">Administration</span>
          </div>
          <div class="navbar-links">
            <a routerLink="/admin" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}" class="nav-link">
              <svg class="icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
              </svg>
              <span>Dashboard</span>
            </a>
            <a routerLink="/admin/audit-logs" routerLinkActive="active" class="nav-link">
              <svg class="icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              <span>Audit Logs</span>
              <span class="badge" *ngIf="totalAuditLogs > 0">{{ totalAuditLogs }}</span>
            </a>
            <a routerLink="/admin/businesses" routerLinkActive="active" class="nav-link">
              <svg class="icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4" />
              </svg>
              <span>Businesses</span>
              <span class="badge" *ngIf="totalBusinesses > 0">{{ totalBusinesses }}</span>
            </a>
          </div>
          <div class="navbar-actions">
            <button (click)="logout()" class="logout-btn">
              <svg class="icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
              <span>Déconnexion</span>
            </button>
          </div>
        </div>
      </nav>

      <!-- Main Content -->
      <div class="admin-content">
        <router-outlet></router-outlet>
      </div>
    </div>
  `,
  styles: [`
    .admin-layout {
      min-height: 100vh;
      background: #f5f7fa;
      display: flex;
      flex-direction: column;
    }

    .admin-navbar {
      background: linear-gradient(135deg, #1e3a8a 0%, #1e40af 100%);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
      position: sticky;
      top: 0;
      z-index: 100;
    }

    .navbar-content {
      max-width: 1400px;
      margin: 0 auto;
      padding: 0;
      display: flex;
      justify-content: space-between;
      align-items: stretch;
      height: 60px;
    }

    .navbar-brand {
      display: flex;
      align-items: center;
      padding: 0 2rem;
      background: rgba(0, 0, 0, 0.1);
    }

    .navbar-title {
      font-size: 1.25rem;
      font-weight: 700;
      color: white;
      letter-spacing: 0.5px;
    }

    .navbar-links {
      display: flex;
      align-items: stretch;
      flex: 1;
      gap: 0;
    }

    .nav-link {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0 1.25rem;
      text-decoration: none;
      color: rgba(255, 255, 255, 0.85);
      font-weight: 500;
      font-size: 0.875rem;
      transition: all 0.2s;
      border-bottom: 3px solid transparent;
      position: relative;
    }

    .nav-link:hover {
      background: rgba(255, 255, 255, 0.1);
      color: white;
    }

    .nav-link.active {
      background: rgba(255, 255, 255, 0.15);
      color: white;
      border-bottom-color: #60a5fa;
    }

    .navbar-actions {
      display: flex;
      align-items: center;
      padding: 0 1rem;
    }

    .badge {
      background: rgba(255, 255, 255, 0.2);
      color: white;
      padding: 0.125rem 0.5rem;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 600;
      margin-left: 0.25rem;
    }

    .badge-warning {
      background: #fbbf24;
      color: #78350f;
    }

    .badge-danger {
      background: #ef4444;
      color: white;
    }

    .logout-btn {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 1rem;
      border: 1px solid rgba(255, 255, 255, 0.3);
      border-radius: 6px;
      background: rgba(255, 255, 255, 0.1);
      color: white;
      font-weight: 500;
      font-size: 0.875rem;
      cursor: pointer;
      transition: all 0.2s;
    }

    .logout-btn:hover {
      background: rgba(255, 255, 255, 0.2);
      border-color: rgba(255, 255, 255, 0.5);
    }

    .icon {
      width: 1.125rem;
      height: 1.125rem;
    }

    .admin-content {
      flex: 1;
      padding: 2rem;
      max-width: 1400px;
      width: 100%;
      margin: 0 auto;
    }
  `]
})
export class AdminLayoutComponent implements OnInit {
  totalAuditLogs = 0;
  totalBusinesses = 0;
  failedLogins = 0;
  securityEvents = 0;

  constructor(
    private adminService: AdminService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    // Charger les logs totaux
    this.adminService.getAuditLogs(0, 1).subscribe(data => {
      this.totalAuditLogs = data.totalElements;
    });

    // Charger les businesses
    this.adminService.getAllBusinesses(0, 1).subscribe(data => {
      this.totalBusinesses = data.totalElements;
    });

    // Charger les �checs de connexion
    this.adminService.getAuditLogsByAction('LOGIN_FAILED', 0, 1).subscribe(data => {
      this.failedLogins = data.totalElements;
    });

    // Charger les �v�nements de s�curit�
    this.adminService.getAuditLogsByAction('UNAUTHORIZED_ACCESS', 0, 1).subscribe(data => {
      this.securityEvents = data.totalElements;
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
