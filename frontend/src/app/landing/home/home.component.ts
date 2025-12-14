import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { NavbarLandingComponent } from '../navbar/navbar-landing.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarLandingComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent {
  features = [
    {
      icon: 'calendar',
      title: 'Gestion simplifiée',
      description: 'Gérez vos rendez-vous en quelques clics. Interface intuitive et rapide.'
    },
    {
      icon: 'bell',
      title: 'Notifications automatiques',
      description: 'Rappels par email pour réduire les absences de 70%.'
    },
    {
      icon: 'users',
      title: 'Gestion des clients',
      description: 'Base de données clients centralisée avec historique complet.'
    },
    {
      icon: 'shield',
      title: 'RGPD compliant',
      description: 'Protection des données conforme aux réglementations européennes.'
    },
    {
      icon: 'chart',
      title: 'Statistiques en temps réel',
      description: 'Tableaux de bord et rapports pour piloter votre activité.'
    },
    {
      icon: 'smartphone',
      title: 'Multi-plateforme',
      description: 'Accessible depuis ordinateur, tablette ou smartphone.'
    }
  ];

  benefits = [
    {
      stat: '70%',
      label: 'Moins d\'absences',
      description: 'Grâce aux rappels automatiques'
    },
    {
      stat: '3h',
      label: 'Économisées par semaine',
      description: 'Automatisation de la gestion'
    },
    {
      stat: '99.9%',
      label: 'Disponibilité',
      description: 'Service fiable et sécurisé'
    }
  ];

  constructor(private router: Router) {}

  navigateToRegister() {
    this.router.navigate(['/register']);
  }

  navigateToPricing() {
    this.router.navigate(['/pricing']);
  }
}
