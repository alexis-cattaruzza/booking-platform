import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NavbarLandingComponent } from '../navbar/navbar-landing.component';

@Component({
  selector: 'app-mentions',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarLandingComponent],
  templateUrl: './mentions.component.html',
  styleUrl: './mentions.component.scss'
})
export class MentionsComponent {}
