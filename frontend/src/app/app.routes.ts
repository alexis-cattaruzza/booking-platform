import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { BookingComponent } from './pages/booking/booking.component';
import { PrivacyPolicyComponent } from './components/legal/privacy-policy.component';
import { TermsOfServiceComponent } from './components/legal/terms-of-service.component';
import { LegalMentionsComponent } from './components/legal/legal-mentions.component';
import { CustomerCancelAppointmentComponent } from './pages/customer-cancel-appointment/customer-cancel-appointment.component';
import { VerifyEmailComponent } from './pages/verify-email/verify-email.component';
import { ForgotPasswordComponent } from './pages/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './pages/reset-password/reset-password.component';
import { AdminLayoutComponent } from './pages/admin/admin-layout.component';
import { AdminDashboardComponent } from './pages/admin/admin-dashboard.component';
import { AuditLogsComponent } from './pages/admin/audit-logs.component';
import { BusinessManagementComponent } from './pages/admin/business-management.component';
import { BusinessGdprComponent } from './pages/business/gdpr/business-gdpr.component';
import { BusinessResetPasswordComponent } from './pages/business/reset-password/business-reset-password.component';

export const routes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'verify-email', component: VerifyEmailComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'business/dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },
  {
    path: 'business/gdpr',
    component: BusinessGdprComponent,
    canActivate: [authGuard]
  },
  {
    path: 'business/reset-password',
    component: BusinessResetPasswordComponent,
    canActivate: [authGuard]
  },
  { path: 'book/:slug', component: BookingComponent },
  { path: 'cancel/:token', component: CustomerCancelAppointmentComponent },
  { path: 'privacy-policy', component: PrivacyPolicyComponent },
  { path: 'terms-of-service', component: TermsOfServiceComponent },
  { path: 'legal-mentions', component: LegalMentionsComponent },
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', component: AdminDashboardComponent },
      { path: 'audit-logs', component: AuditLogsComponent },
      { path: 'businesses', component: BusinessManagementComponent }
    ]
  },
  { path: '**', redirectTo: '/login' }
];
