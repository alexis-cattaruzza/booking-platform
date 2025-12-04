import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-legal-mentions',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div class="max-w-4xl mx-auto bg-white rounded-lg shadow-md p-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-6">Mentions Légales</h1>

        <p class="text-sm text-gray-600 mb-8">Dernière mise à jour : {{ lastUpdate }}</p>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">1. Éditeur du site</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Le site Booking Platform est édité par :
          </p>
          <div class="bg-gray-50 p-4 rounded-lg">
            <p class="text-gray-700 mb-2"><strong>Raison sociale :</strong> [Nom de la société]</p>
            <p class="text-gray-700 mb-2"><strong>Forme juridique :</strong> [SAS / SARL / etc.]</p>
            <p class="text-gray-700 mb-2"><strong>Capital social :</strong> [Montant] €</p>
            <p class="text-gray-700 mb-2"><strong>SIRET :</strong> [Numéro SIRET]</p>
            <p class="text-gray-700 mb-2"><strong>TVA intracommunautaire :</strong> [Numéro TVA]</p>
            <p class="text-gray-700 mb-2"><strong>Siège social :</strong> [Adresse complète]</p>
            <p class="text-gray-700 mb-2"><strong>Téléphone :</strong> [Numéro]</p>
            <p class="text-gray-700 mb-2"><strong>Email :</strong> contact&#64;booking-platform.com</p>
          </div>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">2. Directeur de la publication</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            <strong>Nom :</strong> [Nom du directeur de publication]<br>
            <strong>Fonction :</strong> [Président / Gérant]<br>
            <strong>Email :</strong> contact&#64;booking-platform.com
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">3. Hébergement</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Le site est hébergé par :
          </p>
          <div class="bg-gray-50 p-4 rounded-lg">
            <p class="text-gray-700 mb-2"><strong>Hébergeur :</strong> [Nom de l'hébergeur]</p>
            <p class="text-gray-700 mb-2"><strong>Adresse :</strong> [Adresse de l'hébergeur]</p>
            <p class="text-gray-700 mb-2"><strong>Téléphone :</strong> [Numéro]</p>
            <p class="text-gray-700 mb-2"><strong>Site web :</strong> <a href="#" target="_blank" class="text-blue-600 hover:underline">[URL]</a></p>
          </div>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">4. Protection des données personnelles</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Conformément à la loi "Informatique et Libertés" du 6 janvier 1978 modifiée et au
            Règlement Général sur la Protection des Données (RGPD), vous disposez de droits sur vos données personnelles.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            Pour en savoir plus, consultez notre
            <a [routerLink]="['/privacy-policy']" class="text-blue-600 hover:underline">Politique de Confidentialité</a>.
          </p>
          <div class="bg-gray-50 p-4 rounded-lg">
            <p class="text-gray-700 mb-2"><strong>Responsable du traitement :</strong> Booking Platform</p>
            <p class="text-gray-700 mb-2"><strong>Contact DPO :</strong> privacy&#64;booking-platform.com</p>
            <p class="text-gray-700 mb-2"><strong>Déclaration CNIL :</strong> [Numéro si applicable]</p>
          </div>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">5. Propriété intellectuelle</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            L'ensemble du contenu de ce site (structure, textes, logos, images, vidéos, etc.) est
            la propriété exclusive de Booking Platform ou de ses partenaires.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            Toute reproduction, représentation, modification, publication ou adaptation de tout ou
            partie des éléments du site, quel que soit le moyen ou le procédé utilisé, est interdite
            sans l'autorisation écrite préalable de Booking Platform.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            Toute exploitation non autorisée du site ou de l'un de ses éléments sera considérée comme
            constitutive d'une contrefaçon et poursuivie conformément aux dispositions des articles
            L.335-2 et suivants du Code de Propriété Intellectuelle.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">6. Crédits</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            <strong>Conception et développement :</strong> [Nom de l'agence ou équipe]<br>
            <strong>Design graphique :</strong> [Nom du designer]<br>
            <strong>Photographies :</strong> [Sources des images]
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">7. Cookies</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Ce site utilise des cookies pour améliorer l'expérience utilisateur et réaliser des
            statistiques de visites.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            Vous pouvez gérer vos préférences de cookies via la bannière de consentement affichée
            lors de votre première visite.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            Pour plus d'informations, consultez notre
            <a [routerLink]="['/privacy-policy']" class="text-blue-600 hover:underline">Politique de Confidentialité</a>.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">8. Liens hypertextes</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Ce site peut contenir des liens vers d'autres sites web. Booking Platform décline toute
            responsabilité quant au contenu de ces sites externes.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            La création de liens hypertextes vers le site Booking Platform nécessite une autorisation
            préalable écrite. Les liens doivent pointer vers la page d'accueil et ne doivent pas
            induire en erreur sur la nature des relations avec Booking Platform.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">9. Responsabilité</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Booking Platform s'efforce d'assurer l'exactitude et la mise à jour des informations
            diffusées sur le site, mais ne peut garantir l'absence d'erreurs.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            Booking Platform décline toute responsabilité pour :
          </p>
          <ul class="list-disc list-inside space-y-2 text-gray-700 ml-4">
            <li>Les inexactitudes ou omissions portant sur des informations disponibles sur le site</li>
            <li>Les dommages résultant d'une intrusion frauduleuse d'un tiers ayant entraîné une modification des informations</li>
            <li>Les dommages directs ou indirects résultant de l'accès ou de l'utilisation du site</li>
            <li>L'indisponibilité temporaire ou permanente du site</li>
            <li>Les virus qui pourraient infecter votre équipement informatique</li>
          </ul>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">10. Loi applicable</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Les présentes mentions légales sont régies par la loi française.
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            En cas de litige et à défaut d'accord amiable, le litige sera porté devant les tribunaux
            français conformément aux règles de compétence en vigueur.
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">11. Médiation</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Conformément aux dispositions du Code de la consommation concernant le règlement amiable
            des litiges, Booking Platform adhère au Service du Médiateur du e-commerce de la FEVAD
            (Fédération du e-commerce et de la vente à distance).
          </p>
          <p class="text-gray-700 leading-relaxed mb-4">
            En cas de litige, vous pouvez déposer votre réclamation sur la plateforme de résolution
            en ligne : <a href="https://webgate.ec.europa.eu/odr" target="_blank" class="text-blue-600 hover:underline">https://webgate.ec.europa.eu/odr</a>
          </p>
        </section>

        <section class="mb-8">
          <h2 class="text-2xl font-semibold text-gray-800 mb-4">12. Contact</h2>
          <p class="text-gray-700 leading-relaxed mb-4">
            Pour toute question ou réclamation concernant le site :<br>
            Email : <a href="mailto:contact@booking-platform.com" class="text-blue-600 hover:underline">contact&#64;booking-platform.com</a><br>
            Téléphone : [Numéro]<br>
            Courrier : [Adresse postale complète]
          </p>
        </section>

        <div class="mt-8 pt-6 border-t border-gray-200">
          <a [routerLink]="['/']" class="text-blue-600 hover:underline">← Retour à l'accueil</a>
        </div>
      </div>
    </div>
  `
})
export class LegalMentionsComponent {
  lastUpdate = new Date().toLocaleDateString('fr-FR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
}
