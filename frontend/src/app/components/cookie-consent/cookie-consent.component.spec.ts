import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CookieConsentComponent } from './cookie-consent.component';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PLATFORM_ID } from '@angular/core';

describe('CookieConsentComponent', () => {
  let component: CookieConsentComponent;
  let fixture: ComponentFixture<CookieConsentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CookieConsentComponent, CommonModule, FormsModule],
      providers: [
        { provide: PLATFORM_ID, useValue: 'browser' }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CookieConsentComponent);
    component = fixture.componentInstance;
    localStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show banner when no consent is stored', () => {
    component.ngOnInit();
    expect(component.showBanner).toBe(true);
  });

  it('should not show banner when consent exists', () => {
    localStorage.setItem('cookie-consent', JSON.stringify({
      necessary: true,
      analytics: false,
      marketing: false
    }));

    component.ngOnInit();
    expect(component.showBanner).toBe(false);
  });

  it('should load saved preferences from localStorage', () => {
    const savedPreferences = {
      necessary: true,
      analytics: true,
      marketing: false
    };
    localStorage.setItem('cookie-consent', JSON.stringify(savedPreferences));

    component.ngOnInit();

    expect(component.preferences.analytics).toBe(true);
    expect(component.preferences.marketing).toBe(false);
  });

  describe('acceptAll', () => {
    it('should set all preferences to true', () => {
      component.acceptAll();

      expect(component.preferences.necessary).toBe(true);
      expect(component.preferences.analytics).toBe(true);
      expect(component.preferences.marketing).toBe(true);
    });

    it('should save preferences to localStorage', () => {
      component.acceptAll();

      const saved = localStorage.getItem('cookie-consent');
      expect(saved).toBeTruthy();

      const parsed = JSON.parse(saved!);
      expect(parsed.analytics).toBe(true);
      expect(parsed.marketing).toBe(true);
    });

    it('should hide banner after accepting', () => {
      component.showBanner = true;
      component.acceptAll();

      expect(component.showBanner).toBe(false);
    });

    it('should save consent date', () => {
      component.acceptAll();

      const consentDate = localStorage.getItem('cookie-consent-date');
      expect(consentDate).toBeTruthy();
    });
  });

  describe('acceptNecessary', () => {
    it('should only set necessary cookies to true', () => {
      component.acceptNecessary();

      expect(component.preferences.necessary).toBe(true);
      expect(component.preferences.analytics).toBe(false);
      expect(component.preferences.marketing).toBe(false);
    });

    it('should save minimal preferences to localStorage', () => {
      component.acceptNecessary();

      const saved = localStorage.getItem('cookie-consent');
      const parsed = JSON.parse(saved!);

      expect(parsed.necessary).toBe(true);
      expect(parsed.analytics).toBe(false);
      expect(parsed.marketing).toBe(false);
    });

    it('should hide banner', () => {
      component.showBanner = true;
      component.acceptNecessary();

      expect(component.showBanner).toBe(false);
    });
  });

  describe('saveCustomPreferences', () => {
    it('should save custom cookie preferences', () => {
      component.preferences = {
        necessary: true,
        analytics: true,
        marketing: false
      };

      component.saveCustomPreferences();

      const saved = localStorage.getItem('cookie-consent');
      const parsed = JSON.parse(saved!);

      expect(parsed.analytics).toBe(true);
      expect(parsed.marketing).toBe(false);
    });

    it('should hide details panel', () => {
      component.showDetails = true;
      component.saveCustomPreferences();

      expect(component.showDetails).toBe(false);
    });

    it('should hide banner', () => {
      component.showBanner = true;
      component.saveCustomPreferences();

      expect(component.showBanner).toBe(false);
    });
  });

  describe('toggleDetails', () => {
    it('should toggle details visibility', () => {
      expect(component.showDetails).toBe(false);

      component.toggleDetails();
      expect(component.showDetails).toBe(true);

      component.toggleDetails();
      expect(component.showDetails).toBe(false);
    });
  });

  describe('openPrivacyPolicy', () => {
    it('should open privacy policy in new tab', () => {
      spyOn(window, 'open');

      component.openPrivacyPolicy();

      expect(window.open).toHaveBeenCalledWith('/privacy-policy', '_blank');
    });
  });

  describe('preferences validation', () => {
    it('should always keep necessary cookies enabled', () => {
      component.preferences.necessary = false; // Try to disable
      component.acceptAll();

      expect(component.preferences.necessary).toBe(true);
    });

    it('should allow disabling non-necessary cookies', () => {
      component.preferences = {
        necessary: true,
        analytics: false,
        marketing: false
      };

      component.saveCustomPreferences();

      const saved = JSON.parse(localStorage.getItem('cookie-consent')!);
      expect(saved.analytics).toBe(false);
      expect(saved.marketing).toBe(false);
    });
  });

  describe('SSR compatibility', () => {
    it('should not crash when localStorage is unavailable', () => {
      // Simulate SSR environment
      const originalLocalStorage = localStorage;
      Object.defineProperty(window, 'localStorage', {
        value: undefined,
        writable: true
      });

      expect(() => component.ngOnInit()).not.toThrow();

      // Restore localStorage
      Object.defineProperty(window, 'localStorage', {
        value: originalLocalStorage,
        writable: true
      });
    });
  });

  describe('banner display logic', () => {
    it('should show banner on first visit', () => {
      component.checkConsent();
      expect(component.showBanner).toBe(true);
    });

    it('should not show banner on subsequent visits', () => {
      localStorage.setItem('cookie-consent', JSON.stringify({
        necessary: true,
        analytics: false,
        marketing: false
      }));

      component.checkConsent();
      expect(component.showBanner).toBe(false);
    });

    it('should show banner again if consent is cleared', () => {
      localStorage.setItem('cookie-consent', JSON.stringify({
        necessary: true,
        analytics: true,
        marketing: true
      }));

      component.checkConsent();
      expect(component.showBanner).toBe(false);

      localStorage.removeItem('cookie-consent');
      component.checkConsent();
      expect(component.showBanner).toBe(true);
    });
  });
});
