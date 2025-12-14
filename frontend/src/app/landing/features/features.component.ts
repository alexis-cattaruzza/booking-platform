import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { NavbarLandingComponent } from '../navbar/navbar-landing.component';

interface FeatureDetail {
  icon: string;
  title: string;
  description: string;
  benefits: string[];
}

@Component({
  selector: 'app-features',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarLandingComponent],
  templateUrl: './features.component.html',
  styleUrl: './features.component.scss'
})
export class FeaturesComponent {
  mainFeatures: FeatureDetail[] = [
    {
      icon: 'calendar',
      title: 'Calendrier intelligent',
      description: 'Visualisez et gérez vos rendez-vous en un coup d\'œil avec notre calendrier intuitif',
      benefits: [
        'Vue jour, semaine, mois personnalisable',
        'Glisser-déposer pour déplacer les RDV',
        'Codes couleur par type de service',
        'Synchronisation temps réel',
        'Gestion des créneaux disponibles'
      ]
    },
    {
      icon: 'bell',
      title: 'Notifications automatiques',
      description: 'Réduisez les absences avec des rappels intelligents par email et SMS',
      benefits: [
        'Rappels automatiques 24h avant',
        'Confirmation de RDV par email',
        'SMS de rappel personnalisables',
        'Notifications en temps réel',
        'Historique des communications'
      ]
    },
    {
      icon: 'users',
      title: 'Gestion des clients',
      description: 'Centralisez toutes les informations de vos clients au même endroit',
      benefits: [
        'Fiches clients complètes',
        'Historique des rendez-vous',
        'Notes et préférences',
        'Import/export de données',
        'Recherche avancée'
      ]
    },
    {
      icon: 'chart',
      title: 'Statistiques & Rapports',
      description: 'Analysez votre activité avec des rapports détaillés et en temps réel',
      benefits: [
        'Tableau de bord interactif',
        'Rapports mensuels automatiques',
        'Taux d\'occupation',
        'Chiffre d\'affaires',
        'Export PDF et Excel'
      ]
    },
    {
      icon: 'shield',
      title: 'Sécurité RGPD',
      description: 'Vos données et celles de vos clients sont protégées selon les normes européennes',
      benefits: [
        'Conformité RGPD garantie',
        'Cryptage des données',
        'Sauvegardes automatiques',
        'Droit à l\'oubli',
        'Export de données'
      ]
    },
    {
      icon: 'smartphone',
      title: 'Accessibilité multi-plateformes',
      description: 'Accédez à votre agenda depuis n\'importe quel appareil, n\'importe où',
      benefits: [
        'Interface responsive',
        'Application web progressive',
        'Synchronisation multi-appareils',
        'Mode hors ligne',
        'Notifications push'
      ]
    }
  ];

  additionalFeatures = [
    {
      category: 'Réservation',
      items: [
        'Page de réservation en ligne personnalisée',
        'Widget intégrable sur votre site',
        'Gestion des créneaux et disponibilités',
        'Confirmation automatique',
        'Paiement en ligne (option)'
      ]
    },
    {
      category: 'Organisation',
      items: [
        'Gestion des services et tarifs',
        'Durées personnalisables',
        'Temps de pause entre RDV',
        'Gestion des congés et vacances',
        'Plusieurs calendriers'
      ]
    },
    {
      category: 'Communication',
      items: [
        'Templates d\'emails personnalisables',
        'Rappels SMS automatiques',
        'Confirmation de rendez-vous',
        'Annulation simplifiée',
        'Messages de suivi'
      ]
    },
    {
      category: 'Intégrations',
      items: [
        'Google Calendar',
        'Outlook Calendar',
        'Zapier',
        'API REST complète',
        'Webhooks'
      ]
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
