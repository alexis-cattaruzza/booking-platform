import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-cookie-consent',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cookie-consent.component.html',
  styleUrl: './cookie-consent.component.scss'
})
export class CookieConsentComponent implements OnInit {
  showBanner = false;
  showDetails = false;

  preferences = {
    necessary: true, // Always enabled
    analytics: false,
    marketing: false
  };

  private isBrowser: boolean;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  ngOnInit() {
    this.checkConsent();
  }

  // --------------------------
  // Safe localStorage access
  // --------------------------
  private safeGetItem(key: string): string | null {
    if (!this.isBrowser || !window.localStorage) return null;
    return localStorage.getItem(key);
  }

  private safeSetItem(key: string, value: string): void {
    if (!this.isBrowser || !window.localStorage) return;
    localStorage.setItem(key, value);
  }

  private safeRemoveItem(key: string): void {
    if (!this.isBrowser || !window.localStorage) return;
    localStorage.removeItem(key);
  }

  checkConsent() {
    const consent = this.safeGetItem('cookie-consent');
    if (!consent) {
      this.showBanner = true;
    } else {
      const saved = JSON.parse(consent);
      this.preferences = { ...this.preferences, ...saved };
      this.showBanner = false;
    }
  }

  acceptAll() {
    this.preferences = {
      necessary: true,
      analytics: true,
      marketing: true
    };
    this.savePreferences();
  }

  acceptNecessary() {
    this.preferences = {
      necessary: true,
      analytics: false,
      marketing: false
    };
    this.savePreferences();
  }

  saveCustomPreferences() {
    this.savePreferences();
  }

  private savePreferences() {
    this.safeSetItem('cookie-consent', JSON.stringify(this.preferences));
    this.safeSetItem('cookie-consent-date', new Date().toISOString());
    this.showBanner = false;
    this.showDetails = false;
    this.applyPreferences();
  }

  private applyPreferences() {
    console.log(`Analytics cookies ${this.preferences.analytics ? 'enabled' : 'disabled'}`);
    console.log(`Marketing cookies ${this.preferences.marketing ? 'enabled' : 'disabled'}`);
  }

  toggleDetails() {
    this.showDetails = !this.showDetails;
  }

  openPrivacyPolicy() {
    if (this.isBrowser) {
      window.open('/privacy-policy', '_blank');
    }
  }
}
