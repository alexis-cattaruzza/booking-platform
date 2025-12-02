import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { CookieConsentComponent } from './components/cookie-consent/cookie-consent.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CookieConsentComponent],
  templateUrl: './app.component.html',
})
export class AppComponent {
  title = 'frontend';
}
