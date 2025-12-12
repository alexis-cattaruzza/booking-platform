import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CookieConsentComponent } from './components/cookie-consent/cookie-consent.component';
import { EmailVerificationBannerComponent } from './shared/email-verification-banner/email-verification-banner.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CookieConsentComponent, EmailVerificationBannerComponent],
  templateUrl: './app.component.html',
})
export class AppComponent {
  title = 'frontend';
}
