import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { NavbarLandingComponent } from '../navbar/navbar-landing.component';

interface PricingPlan {
  name: string;
  price: number;
  priceYearly: number;
  description: string;
  features: string[];
  limitations?: string[];
  highlighted?: boolean;
  cta: string;
}

@Component({
  selector: 'app-pricing',
  standalone: true,
  imports: [CommonModule, RouterModule, NavbarLandingComponent],
  templateUrl: './pricing.component.html',
  styleUrl: './pricing.component.scss'
})
export class PricingComponent {
  isYearly = false;

  plans: PricingPlan[] = [
    {
      name: 'Gratuit',
      price: 0,
      priceYearly: 0,
      description: 'Parfait pour tester la plateforme',
      features: [
        'Jusqu\'à 15 rendez-vous par mois',
        '1 utilisateur',
        'Gestion de base des RDV',
        'Calendrier simple',
        'Support par email (72h)'
      ],
      limitations: [
        'Fonctionnalités limitées',
        'Pas de rappels SMS',
        'Pas de statistiques avancées'
      ],
      cta: 'Commencer gratuitement'
    },
    {
      name: 'Starter',
      price: 19,
      priceYearly: 15,
      description: 'Idéal pour les indépendants',
      features: [
        'Jusqu\'à 200 rendez-vous par mois',
        '1 utilisateur',
        'Toutes les fonctionnalités de base',
        'Notifications email illimitées',
        '50 SMS de rappel par mois',
        'Calendrier avancé',
        'Support par email (24h)',
        'Personnalisation basique'
      ],
      highlighted: true,
      cta: 'Essayer 14 jours gratuits'
    },
    {
      name: 'Pro',
      price: 49,
      priceYearly: 39,
      description: 'Pour les professionnels exigeants',
      features: [
        'Rendez-vous illimités',
        'Jusqu\'à 5 utilisateurs',
        'Toutes les fonctionnalités Starter',
        'SMS illimités',
        'Statistiques et rapports avancés',
        'Intégrations (Google Calendar, etc.)',
        'Page de réservation personnalisée',
        'Support prioritaire (4h)',
        'Gestion des vacances et congés',
        'Export des données'
      ],
      cta: 'Essayer 14 jours gratuits'
    },
    {
      name: 'Enterprise',
      price: 0,
      priceYearly: 0,
      description: 'Solution sur mesure',
      features: [
        'Tout du plan Pro',
        'Utilisateurs illimités',
        'Onboarding personnalisé',
        'Support dédié 24/7',
        'SLA garanti 99.9%',
        'Accès API complet',
        'Formation de l\'équipe',
        'Tarification sur devis'
      ],
      cta: 'Nous contacter'
    }
  ];

  faqs = [
    {
      question: 'Puis-je changer de plan à tout moment ?',
      answer: 'Oui, vous pouvez upgrader ou downgrader votre plan à tout moment. Les changements sont effectifs immédiatement et le montant est ajusté au prorata.'
    },
    {
      question: 'La période d\'essai nécessite-t-elle une carte bancaire ?',
      answer: 'Non, vous pouvez tester gratuitement pendant 14 jours sans fournir de carte bancaire. Vous ne serez facturé qu\'après avoir choisi un plan payant.'
    },
    {
      question: 'Que se passe-t-il si je dépasse ma limite de rendez-vous ?',
      answer: 'Nous vous préviendrons par email lorsque vous approchez de votre limite. Vous pourrez alors upgrader votre plan ou attendre le mois suivant.'
    },
    {
      question: 'Puis-je annuler mon abonnement ?',
      answer: 'Oui, vous pouvez annuler à tout moment. Aucun engagement, aucune pénalité. Votre accès reste actif jusqu\'à la fin de la période payée.'
    },
    {
      question: 'Proposez-vous des réductions pour les paiements annuels ?',
      answer: 'Oui ! En choisissant la facturation annuelle, vous économisez 20% sur le tarif mensuel, soit l\'équivalent de 2 mois gratuits.'
    },
    {
      question: 'Les prix incluent-ils la TVA ?',
      answer: 'Les prix affichés sont hors taxes. La TVA applicable (20% en France) sera ajoutée lors du paiement selon votre pays de résidence.'
    }
  ];

  constructor(private router: Router) {}

  toggleBilling() {
    this.isYearly = !this.isYearly;
  }

  selectPlan(plan: PricingPlan) {
    if (plan.name === 'Enterprise') {
      this.router.navigate(['/contact']);
    } else {
      this.router.navigate(['/register'], {
        queryParams: { plan: plan.name.toLowerCase() }
      });
    }
  }

  getPrice(plan: PricingPlan): number {
    return this.isYearly ? plan.priceYearly : plan.price;
  }

  getSavings(plan: PricingPlan): number {
    if (plan.price === 0) return 0;
    return Math.round(((plan.price * 12 - plan.priceYearly * 12) / (plan.price * 12)) * 100);
  }
}
