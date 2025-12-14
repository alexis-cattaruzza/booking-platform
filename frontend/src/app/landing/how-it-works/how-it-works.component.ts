import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { NavbarLandingComponent } from '../navbar/navbar-landing.component';

interface Step {
  number: number;
  title: string;
  description: string;
  details: string[];
}

@Component({
  selector: 'app-how-it-works',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarLandingComponent],
  templateUrl: './how-it-works.component.html',
  styleUrl: './how-it-works.component.scss'
})
export class HowItWorksComponent {
  steps: Step[] = [
    {
      number: 1,
      title: 'Créez votre compte',
      description: 'Inscrivez-vous gratuitement en moins de 2 minutes',
      details: [
        'Aucune carte bancaire requise',
        'Configuration guidée pas à pas',
        'Import de vos données existantes (optionnel)'
      ]
    },
    {
      number: 2,
      title: 'Configurez vos services',
      description: 'Définissez vos prestations, durées et tarifs',
      details: [
        'Ajoutez vos services et catégories',
        'Définissez les durées et prix',
        'Configurez vos horaires de travail'
      ]
    },
    {
      number: 3,
      title: 'Partagez votre lien',
      description: 'Recevez vos premières réservations',
      details: [
        'Obtenez votre page de réservation personnalisée',
        'Partagez le lien à vos clients',
        'Intégrez le widget sur votre site web'
      ]
    },
    {
      number: 4,
      title: 'Gérez en toute simplicité',
      description: 'Suivez vos rendez-vous et développez votre activité',
      details: [
        'Notifications automatiques',
        'Statistiques en temps réel',
        'Support client dédié'
      ]
    }
  ];

  constructor(private router: Router) {}

  navigateToRegister() {
    this.router.navigate(['/register']);
  }
}
