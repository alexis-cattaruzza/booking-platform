import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
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

  ngOnInit() {
    this.checkConsent();
  }

  checkConsent() {
    const consent = localStorage.getItem('cookie-consent');
    if (!consent) {
      // Show banner if no consent recorded
      this.showBanner = true;
    } else {
      // Load saved preferences
      const saved = JSON.parse(consent);
      this.preferences = { ...this.preferences, ...saved };
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
    localStorage.setItem('cookie-consent', JSON.stringify(this.preferences));
    localStorage.setItem('cookie-consent-date', new Date().toISOString());
    this.showBanner = false;
    this.showDetails = false;

    // Apply preferences (enable/disable analytics, marketing cookies)
    this.applyPreferences();
  }

  private applyPreferences() {
    // Here you would enable/disable analytics tools like Google Analytics
    if (this.preferences.analytics) {
      console.log('Analytics cookies enabled');
      // Example: Initialize Google Analytics
    } else {
      console.log('Analytics cookies disabled');
      // Example: Disable Google Analytics
    }

    if (this.preferences.marketing) {
      console.log('Marketing cookies enabled');
      // Example: Initialize Facebook Pixel, etc.
    } else {
      console.log('Marketing cookies disabled');
    }
  }

  toggleDetails() {
    this.showDetails = !this.showDetails;
  }

  openPrivacyPolicy() {
    window.open('/privacy-policy', '_blank');
  }
}
