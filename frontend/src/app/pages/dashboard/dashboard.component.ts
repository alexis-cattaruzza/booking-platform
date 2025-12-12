import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { UserInfo } from '../../models/auth.model';
import { BusinessProfileComponent } from '../../components/business-profile/business-profile.component';
import { ServicesManagementComponent } from '../../components/services-management/services-management.component';
import { SchedulesManagementComponent } from '../../components/schedules-management/schedules-management.component';
import { AppointmentsManagementComponent } from '../../components/appointments-management/appointments-management.component';
import { VacationManagementComponent } from '../admin/vacation-management.component';
import { DeletionBannerComponent } from '../../shared/deletion-banner/deletion-banner.component';
import { ServiceService } from '../../services/service.service';
import { BusinessService } from '../../services/business.service';
import { Service, Business } from '../../models/business.model';

type TabType = 'overview' | 'profile' | 'services' | 'schedules' | 'appointments' | 'vacations';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, BusinessProfileComponent, ServicesManagementComponent, SchedulesManagementComponent, AppointmentsManagementComponent, VacationManagementComponent, DeletionBannerComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  user$: Observable<UserInfo | null>;
  activeTab: TabType = 'overview';
  business: Business | null = null;

  stats = {
    appointmentsToday: 0,
    activeServices: 0,
    customersThisMonth: 0,
    pendingAppointments: 0
  };

  tabs = [
    { id: 'overview' as TabType, name: 'Vue d\'ensemble', icon: '📊' },
    { id: 'profile' as TabType, name: 'Mon Profil', icon: '🏢' },
    { id: 'services' as TabType, name: 'Services', icon: '💼' },
    { id: 'schedules' as TabType, name: 'Horaires', icon: '📅' },
    { id: 'appointments' as TabType, name: 'Rendez-vous', icon: '📋' },
    { id: 'vacations' as TabType, name: 'Vacances', icon: '🏖️' }
  ];

  constructor(
    private authService: AuthService,
    private router: Router,
    private serviceService: ServiceService,
    private businessService: BusinessService
  ) {
    this.user$ = this.authService.currentUser;
  }

  ngOnInit() {
    this.loadStats();
    this.loadBusiness();
  }

  loadBusiness() {
    this.businessService.getMyBusiness().subscribe({
      next: (business) => {
        this.business = business;
      },
      error: (err) => {
        console.error('Error loading business:', err);
      }
    });
  }

  loadStats() {
    this.serviceService.getServices().subscribe({
      next: (services: Service[]) => {
        this.stats.activeServices = services.filter((s: Service) => s.isActive).length;
      }
    });
  }

  setActiveTab(tab: TabType) {
    this.activeTab = tab;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
