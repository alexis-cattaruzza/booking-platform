import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-privacy-policy',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div class="max-w-4xl mx-auto bg-white rounded-lg shadow-md p-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-6">Politique de Confidentialité</h1>

        <p class="text-sm text-gray-600 mb-8">Dernière mise à jour : {{ lastUpdate }}</p>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">1. Introduction</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Booking Platform s'engage à protéger la confidentialité de vos données personnelles.
            Cette politique de confidentialité explique comment nous collectons, utilisons, stockons
            et protégeons vos informations conformément au Règlement Général sur la Protection des
            Données (RGPD) et à la loi Informatique et Libertés.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">2. Responsable du traitement</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Le responsable du traitement des données personnelles est Booking Platform.<br>
            Email de contact : contact&#64;booking-platform.com
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">3. Données collectées</h2>
          <p class="text-gray-700 leading-relaxed mb-4">Nous collectons les données suivantes :</p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li><strong>Pour les clients :</strong> nom, prénom, email, numéro de téléphone, historique des rendez-vous</li>
            <li><strong>Pour les professionnels :</strong> informations d'entreprise (nom, SIRET, adresse), services proposés, horaires d'ouverture</li>
            <li><strong>Données de connexion :</strong> adresse IP, logs de connexion, cookies</li>
            <li><strong>Données de paiement :</strong> informations de facturation (traitées par notre prestataire de paiement sécurisé)</li>
          </ul>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">4. Finalités du traitement</h2>
          <p class="text-gray-700 leading-relaxed mb-4">Vos données sont utilisées pour :</p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li>La gestion des rendez-vous et des réservations</li>
            <li>L'envoi de notifications et rappels de rendez-vous</li>
            <li>La gestion de votre compte utilisateur</li>
            <li>L'amélioration de nos services</li>
            <li>Le respect de nos obligations légales et réglementaires</li>
            <li>La prévention de la fraude et la sécurité de la plateforme</li>
          </ul>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">5. Base légale du traitement</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Le traitement de vos données repose sur :
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li><strong>L'exécution du contrat :</strong> gestion des rendez-vous et services</li>
            <li><strong>Votre consentement :</strong> cookies non essentiels, newsletters</li>
            <li><strong>Nos intérêts légitimes :</strong> amélioration des services, sécurité</li>
            <li><strong>Obligations légales :</strong> conservation des données comptables, lutte contre la fraude</li>
          </ul>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">6. Durée de conservation</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Vos données sont conservées pendant la durée nécessaire aux finalités pour lesquelles
            elles sont collectées :
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li><strong>Données de compte :</strong> jusqu'à la suppression de votre compte + 1 mois</li>
            <li><strong>Historique des rendez-vous :</strong> 3 ans après le dernier rendez-vous</li>
            <li><strong>Données comptables :</strong> 10 ans (obligation légale)</li>
            <li><strong>Cookies :</strong> 13 mois maximum</li>
          </ul>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">7. Vos droits (RGPD)</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Conformément au RGPD, vous disposez des droits suivants :
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li><strong>Droit d'accès :</strong> obtenir une copie de vos données personnelles</li>
            <li><strong>Droit de rectification :</strong> corriger vos données inexactes</li>
            <li><strong>Droit à l'effacement :</strong> supprimer vos données ("droit à l'oubli")</li>
            <li><strong>Droit à la portabilité :</strong> recevoir vos données dans un format structuré</li>
            <li><strong>Droit d'opposition :</strong> vous opposer au traitement de vos données</li>
            <li><strong>Droit à la limitation :</strong> limiter le traitement de vos données</li>
          </ul>
          <p class="text-gray-700 leading-relaxed mt-4">
            Pour exercer vos droits, contactez-nous à : <a href="mailto:privacy@booking-platform.com" class="text-blue-600 hover:underline">privacy&#64;booking-platform.com</a>
            ou directement depuis votre compte dans les paramètres.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">8. Sécurité des données</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Nous mettons en œuvre des mesures techniques et organisationnelles pour protéger vos données :
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li>Chiffrement des données sensibles (SSL/TLS)</li>
            <li>Authentification sécurisée (JWT tokens)</li>
            <li>Accès restreint aux données personnelles</li>
            <li>Sauvegardes régulières et sécurisées</li>
            <li>Surveillance et détection des incidents de sécurité</li>
          </ul>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">9. Cookies</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Notre site utilise des cookies pour améliorer votre expérience. Vous pouvez gérer vos
            préférences de cookies à tout moment via la bannière de consentement.
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li><strong>Cookies essentiels :</strong> nécessaires au fonctionnement du site (authentification, sécurité)</li>
            <li><strong>Cookies analytiques :</strong> mesure d'audience et statistiques (avec votre consentement)</li>
            <li><strong>Cookies marketing :</strong> publicité ciblée (avec votre consentement)</li>
          </ul>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">10. Partage des données</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Vos données peuvent être partagées avec :
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li><strong>Les professionnels :</strong> lorsque vous prenez un rendez-vous</li>
            <li><strong>Prestataires de services :</strong> hébergement, email, paiement (sous contrat de confidentialité)</li>
            <li><strong>Autorités légales :</strong> si requis par la loi</li>
          </ul>
          <p class="text-gray-700 leading-relaxed mt-4">
            Nous ne vendons jamais vos données personnelles à des tiers.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">11. Transferts internationaux</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Vos données sont hébergées au sein de l'Union Européenne. En cas de transfert hors UE,
            nous garantissons un niveau de protection adéquat (clauses contractuelles types, Privacy Shield).
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">12. Modifications</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Nous nous réservons le droit de modifier cette politique de confidentialité.
            Toute modification sera publiée sur cette page avec une nouvelle date de mise à jour.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">13. Réclamation</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Si vous estimez que vos droits ne sont pas respectés, vous pouvez introduire une réclamation
            auprès de la CNIL (Commission Nationale de l'Informatique et des Libertés) :
          </p>
          <p class="text-gray-700 leading-relaxed">
            CNIL - 3 Place de Fontenoy - TSA 80715 - 75334 PARIS CEDEX 07<br>
            Téléphone : 01 53 73 22 22<br>
            Site web : <a href="https://www.cnil.fr" target="_blank" class="text-blue-600 hover:underline">www.cnil.fr</a>
          </p>
        </section>

        <div class="mt-8 pt-6 border-t border-gray-200">
          <a [routerLink]="['/']" class="text-blue-600 hover:underline">← Retour à l'accueil</a>
        </div>
      </div>
    </div>
  `
})
export class PrivacyPolicyComponent {
  lastUpdate = new Date().toLocaleDateString('fr-FR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
}
