import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../services/auth.service';
import { UserInfo } from '../../models/auth.model';

type TabType = 'overview' | 'profile' | 'services' | 'schedules' | 'appointments';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit {
  user$: Observable<UserInfo | null>;
  activeTab: TabType = 'overview';

  tabs = [
    { id: 'overview' as TabType, name: 'Vue d\'ensemble', icon: '📊' },
    { id: 'profile' as TabType, name: 'Mon Profil', icon: '🏢' },
    { id: 'services' as TabType, name: 'Services', icon: '💼' },
    { id: 'schedules' as TabType, name: 'Horaires', icon: '📅' },
    { id: 'appointments' as TabType, name: 'Rendez-vous', icon: '📋' }
  ];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {
    this.user$ = this.authService.currentUser;
  }

  ngOnInit() {}

  setActiveTab(tab: TabType) {
    this.activeTab = tab;
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
