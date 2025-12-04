import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-terms-of-service',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div class="max-w-4xl mx-auto bg-white rounded-lg shadow-md p-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-6">Conditions Générales d'Utilisation</h1>

        <p class="text-sm text-gray-600 mb-8">Dernière mise à jour : {{ lastUpdate }}</p>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">1. Objet</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Les présentes Conditions Générales d'Utilisation (CGU) ont pour objet de définir les
            modalités et conditions d'utilisation de la plateforme Booking Platform, ainsi que les
            droits et obligations des utilisateurs.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            L'utilisation de la plateforme implique l'acceptation pleine et entière des présentes CGU.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">2. Définitions</h2>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li><strong>Plateforme :</strong> le site web Booking Platform et ses services associés</li>
            <li><strong>Utilisateur :</strong> toute personne utilisant la plateforme</li>
            <li><strong>Client :</strong> utilisateur prenant rendez-vous via la plateforme</li>
            <li><strong>Professionnel :</strong> prestataire de services proposant des rendez-vous</li>
            <li><strong>Rendez-vous :</strong> réservation d'un service auprès d'un professionnel</li>
          </ul>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">3. Accès à la plateforme</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            La plateforme est accessible gratuitement à tout utilisateur disposant d'un accès à Internet.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            Nous nous réservons le droit de :
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li>Suspendre ou interrompre temporairement l'accès pour maintenance</li>
            <li>Modifier ou supprimer des fonctionnalités sans préavis</li>
            <li>Refuser l'accès à tout utilisateur ne respectant pas les CGU</li>
          </ul>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">4. Création de compte</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Pour utiliser certains services, la création d'un compte est nécessaire. L'utilisateur s'engage à :
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li>Fournir des informations exactes et à jour</li>
            <li>Maintenir la confidentialité de ses identifiants</li>
            <li>Ne pas créer plusieurs comptes pour la même personne</li>
            <li>Informer immédiatement en cas d'utilisation non autorisée de son compte</li>
          </ul>
          <p class="text-gray-700 leading-relaxed mt-4">
            L'utilisateur est responsable de toutes les activités effectuées depuis son compte.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">5. Services proposés</h2>
          <h3 class="text-xl font-semibold text-gray-800 mb-3">5.1 Pour les clients</h3>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4 mb-4">
            <li>Recherche de professionnels et services</li>
            <li>Prise de rendez-vous en ligne</li>
            <li>Gestion de l'historique des rendez-vous</li>
            <li>Réception de notifications et rappels</li>
            <li>Annulation de rendez-vous (selon conditions)</li>
          </ul>

          <h3 class="text-xl font-semibold text-gray-800 mb-3">5.2 Pour les professionnels</h3>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li>Création et gestion du profil professionnel</li>
            <li>Configuration des services et tarifs</li>
            <li>Gestion du calendrier et des disponibilités</li>
            <li>Gestion des rendez-vous clients</li>
            <li>Outils de communication avec les clients</li>
          </ul>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">6. Rendez-vous</h2>
          <h3 class="text-xl font-semibold text-gray-800 mb-3">6.1 Prise de rendez-vous</h3>
          <p class="text-gray-700 leading-relaxed mb-4">
            Le client peut réserver un rendez-vous en sélectionnant un créneau disponible.
            Une confirmation est envoyée par email après validation.
          </p>

          <h3 class="text-xl font-semibold text-gray-800 mb-3">6.2 Annulation</h3>
          <p class="text-gray-700 leading-relaxed mb-4">
            Les annulations doivent être effectuées dans les délais suivants :
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li><strong>Par le client :</strong> au moins 24h avant le rendez-vous (sauf urgence médicale justifiée)</li>
            <li><strong>Par le professionnel :</strong> notification immédiate au client avec proposition de report</li>
          </ul>
          <p class="text-gray-700 leading-relaxed mt-4">
            Toute annulation tardive ou absence non justifiée peut entraîner des pénalités selon
            les conditions du professionnel.
          </p>

          <h3 class="text-xl font-semibold text-gray-800 mb-3">6.3 Modification</h3>
          <p class="text-gray-700 leading-relaxed mb-4">
            Les modifications de rendez-vous sont possibles sous réserve de disponibilité et d'accord
            entre le client et le professionnel.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">7. Tarification et paiement</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Les tarifs des services sont fixés librement par chaque professionnel et affichés sur
            leur profil.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            Le paiement s'effectue selon les modalités définies par le professionnel :
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li>Paiement sur place (espèces, carte bancaire, chèque)</li>
            <li>Paiement en ligne via la plateforme (si activé)</li>
          </ul>
          <p class="text-gray-700 leading-relaxed mt-4">
            Booking Platform perçoit une commission sur les rendez-vous réservés via la plateforme,
            selon les conditions du contrat professionnel.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">8. Obligations des utilisateurs</h2>
          <h3 class="text-xl font-semibold text-gray-800 mb-3">8.1 Obligations générales</h3>
          <p class="text-gray-700 leading-relaxed mb-4">
            L'utilisateur s'engage à :
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li>Utiliser la plateforme de manière loyale et conforme à sa destination</li>
            <li>Ne pas porter atteinte aux droits des tiers</li>
            <li>Ne pas diffuser de contenu illicite, diffamatoire ou préjudiciable</li>
            <li>Respecter les lois et règlements en vigueur</li>
            <li>Ne pas tenter de contourner les mesures de sécurité</li>
          </ul>

          <h3 class="text-xl font-semibold text-gray-800 mb-3">8.2 Obligations spécifiques des professionnels</h3>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li>Fournir des informations exactes sur leurs services et qualifications</li>
            <li>Maintenir à jour leur calendrier de disponibilités</li>
            <li>Honorer les rendez-vous confirmés</li>
            <li>Respecter les règles déontologiques de leur profession</li>
            <li>Disposer des assurances professionnelles requises</li>
          </ul>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">9. Responsabilité</h2>
          <h3 class="text-xl font-semibold text-gray-800 mb-3">9.1 Responsabilité de Booking Platform</h3>
          <p class="text-gray-700 leading-relaxed mb-4">
            Booking Platform est un intermédiaire technique facilitant la mise en relation entre
            clients et professionnels.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            Nous ne sommes pas responsables de :
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li>La qualité des services fournis par les professionnels</li>
            <li>Les litiges entre clients et professionnels</li>
            <li>Les dommages indirects résultant de l'utilisation de la plateforme</li>
            <li>L'interruption temporaire ou définitive des services</li>
          </ul>

          <h3 class="text-xl font-semibold text-gray-800 mb-3">9.2 Responsabilité des professionnels</h3>
          <p class="text-gray-700 leading-relaxed mb-4">
            Les professionnels sont seuls responsables :
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li>De la qualité et de la conformité de leurs prestations</li>
            <li>Du respect des règles applicables à leur activité</li>
            <li>Des dommages causés aux clients dans le cadre de leurs prestations</li>
          </ul>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">10. Propriété intellectuelle</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            L'ensemble du contenu de la plateforme (textes, images, logos, design) est protégé par
            les droits de propriété intellectuelle.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            Toute reproduction, représentation ou exploitation sans autorisation est interdite.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">11. Données personnelles</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Le traitement des données personnelles est régi par notre
            <a [routerLink]="['/privacy-policy']" class="text-blue-600 hover:underline">Politique de Confidentialité</a>,
            conforme au RGPD.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">12. Suspension et résiliation</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Nous nous réservons le droit de suspendre ou résilier l'accès d'un utilisateur en cas de :
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li>Non-respect des présentes CGU</li>
            <li>Utilisation frauduleuse de la plateforme</li>
            <li>Comportement nuisible envers d'autres utilisateurs</li>
            <li>Décision de justice</li>
          </ul>
          <p class="text-gray-700 leading-relaxed mt-4">
            L'utilisateur peut supprimer son compte à tout moment depuis ses paramètres.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">13. Modification des CGU</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Nous nous réservons le droit de modifier les présentes CGU à tout moment.
            Les utilisateurs seront informés des modifications importantes par email.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            La poursuite de l'utilisation de la plateforme après modification vaut acceptation des nouvelles CGU.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">14. Litiges</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            En cas de litige, nous vous invitons à nous contacter en priorité pour trouver une solution amiable.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            À défaut de règlement amiable, tout litige relève de la compétence des tribunaux français,
            conformément aux règles de droit commun.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">15. Contact</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Pour toute question concernant les CGU :<br>
            Email : <a href="mailto:contact@booking-platform.com" class="text-blue-600 hover:underline">contact&#64;booking-platform.com</a>
          </p>
        </section>

        <div class="mt-8 pt-6 border-t border-gray-200">
          <a [routerLink]="['/']" class="text-blue-600 hover:underline">← Retour à l'accueil</a>
        </div>
      </div>
    </div>
  `
})
export class TermsOfServiceComponent {
  lastUpdate = new Date().toLocaleDateString('fr-FR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
}
