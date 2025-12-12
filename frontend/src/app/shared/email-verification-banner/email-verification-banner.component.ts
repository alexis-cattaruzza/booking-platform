import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-email-verification-banner',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './email-verification-banner.component.html',
  styleUrl: './email-verification-banner.component.scss'
})
export class EmailVerificationBannerComponent implements OnInit {
  showBanner = false;
  isResending = false;
  successMessage = '';

  // Public routes where banner should NOT be displayed
  private publicRoutes = ['/login', '/register', '/booking', '/reset-password', '/verify-email', '/'];

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.checkBannerVisibility();

    // Listen to route changes to update banner visibility
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.checkBannerVisibility();
    });
  }

  private checkBannerVisibility(): void {
    const user = this.authService.currentUserValue;
    const currentUrl = this.router.url;

    // Check if user is not verified
    const isUserUnverified = !!(user && !user.emailVerified);

    // Check if current route is public (should not show banner)
    const isPublicRoute = this.publicRoutes.some(route =>
      currentUrl === route || currentUrl.startsWith(route + '/')
    );

    // Show banner only if user is unverified AND on a private route
    this.showBanner = isUserUnverified && !isPublicRoute;
  }

  resendVerification(): void {
    const user = this.authService.currentUserValue;
    if (!user) return;

    this.isResending = true;
    this.authService.resendVerificationEmail(user.email).subscribe({
      next: () => {
        this.successMessage = 'Email de vérification envoyé !';
        setTimeout(() => this.successMessage = '', 5000);
        this.isResending = false;
      },
      error: () => {
        this.isResending = false;
      }
    });
  }

  closeBanner(): void {
    this.showBanner = false;
  }
}
