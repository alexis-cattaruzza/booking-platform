# 🏗️ ARCHITECTURE - BOOKING PLATFORM

> Plateforme SaaS de réservation pour PME locales  
> Alternative française simple à Calendly

**Version :** 1.0  
**Date :** 25 novembre 2024  
**Auteur :** Alexis

---

## 📋 TABLE DES MATIÈRES

1. [Vue d'ensemble](#vue-densemble)
2. [Stack technique](#stack-technique)
3. [Architecture système](#architecture-système)
4. [Modèle de données](#modèle-de-données)
5. [API REST](#api-rest)
6. [Flux utilisateurs](#flux-utilisateurs)
7. [Sécurité](#sécurité)
8. [Déploiement](#déploiement)
9. [Structure du projet](#structure-du-projet)

---

## 🎯 VUE D'ENSEMBLE

### Problème résolu

Les PME locales (coiffeurs, ostéopathes, garages, coaches) reçoivent leurs réservations de manière désorganisée :
- ❌ Téléphone qui sonne en plein rendez-vous
- ❌ Messages WhatsApp perdus
- ❌ Commentaires Facebook oubliés
- ❌ Agenda papier non synchronisé

### Solution

Une plateforme de réservation en ligne **simple**, **rapide** et **française** :
- ✅ Page de réservation publique unique : `reservez.app/coiffeur-marie`
- ✅ Calendrier de disponibilités en temps réel
- ✅ Notifications automatiques (email + SMS)
- ✅ Dashboard pro pour gérer les rendez-vous
- ✅ Zéro configuration technique pour le client

### Cible

**PME locales françaises** avec 1 à 10 employés :
- Coiffeurs, barbiers, salons de beauté
- Ostéopathes, kinés, médecins
- Garages automobiles
- Coachs sportifs, professeurs particuliers
- Tatoueurs, esthéticiennes

### Modèle économique

**Freemium SaaS** :
- **Gratuit** : 1 service, 20 RDV/mois, branding "Powered by"
- **Starter 15€/mois** : Services illimités, notifications email, sans branding
- **Pro 29€/mois** : + SMS, analytics, Google Calendar sync

**Objectif :** 50 clients payants = 750€/mois MRR en 6-12 mois

---

## 🛠️ STACK TECHNIQUE

### Frontend

| Technologie | Version | Usage |
|-------------|---------|-------|
| **Angular** | 18.x | Framework principal |
| **TypeScript** | 5.x | Langage |
| **Tailwind CSS** | 3.x | Styling |
| **Angular Material** | 18.x | Composants UI |
| **FullCalendar** | 6.x | Widget calendrier |
| **RxJS** | 7.x | Programmation réactive |
| **date-fns** | 3.x | Manipulation dates |

**Fonctionnalités activées :**
- ✅ SSR (Server-Side Rendering) pour SEO
- ✅ Routing
- ✅ Lazy loading
- ✅ Standalone components

### Backend

| Technologie | Version | Usage |
|-------------|---------|-------|
| **Java** | 21 LTS | Langage |
| **Spring Boot** | 3.4.0 | Framework |
| **Spring Security** | 6.x | Authentification JWT |
| **Spring Data JPA** | 3.x | ORM |
| **Hibernate** | 6.x | Persistance |
| **Lombok** | 1.18.x | Réduction boilerplate |
| **MapStruct** | 1.5.x | Mapping DTO |
| **Flyway** | 10.x | Migrations DB |
| **Twilio SDK** | Latest | SMS (à venir) |

### Base de données

| Technologie | Version | Usage |
|-------------|---------|-------|
| **PostgreSQL** | 16+ | Base principale |
| **Redis** | 7.x | Cache & sessions |

### Infrastructure

| Service | Plan | Usage | Coût |
|---------|------|-------|------|
| **Vercel** | Hobby | Frontend (SSR) | Gratuit (100GB/mois) |
| **Railway** | Free | Backend | $5 crédit/mois |
| **Supabase** | Free | PostgreSQL | Gratuit (500MB) |
| **Upstash Redis** | Free | Cache | Gratuit (10K cmd/jour) |
| **Cloudflare** | Free | CDN + DNS | Gratuit illimité |
| **Gmail SMTP** | Free | Emails | Gratuit (500/jour) |

**Coût total estimé :** < 5€/mois au démarrage

---

## 🏗️ ARCHITECTURE SYSTÈME

### Architecture globale (3-tier)

```
┌─────────────────────────────────────────────────────────┐
│                    USERS / CLIENTS                      │
└─────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│               FRONTEND (Angular 18 + SSR)               │
│  ┌────────────────────────────────────────────────┐    │
│  │  Landing Page (SSR)                            │    │
│  │  Dashboard Business (CSR)                      │    │
│  │  Page Réservation Publique (SSR)              │    │
│  └────────────────────────────────────────────────┘    │
│               Deployed on Vercel (Serverless)           │
└─────────────────────────────────────────────────────────┘
                            │
                    HTTPS / REST API
                            │
                            ▼
┌─────────────────────────────────────────────────────────┐
│           BACKEND (Spring Boot 3.4 + Java 21)           │
│  ┌────────────────────────────────────────────────┐    │
│  │  Controllers (REST endpoints)                  │    │
│  │  Services (Business logic)                     │    │
│  │  Repositories (Data access)                    │    │
│  │  Security (JWT auth)                           │    │
│  └────────────────────────────────────────────────┘    │
│            Deployed on Railway (Docker)                 │
└─────────────────────────────────────────────────────────┘
                            │
                    ┌───────┴───────┐
                    ▼               ▼
        ┌───────────────────┐  ┌──────────────┐
        │   PostgreSQL 16   │  │   Redis 7    │
        │   (Supabase)      │  │   (Upstash)  │
        │   Data principale │  │   Cache      │
        └───────────────────┘  └──────────────┘
```

### Architecture backend détaillée

```
Backend (Spring Boot)
├── Controllers (REST API)
│   ├── AuthController          → /api/auth/*
│   ├── BusinessController      → /api/businesses/*
│   ├── ServiceController       → /api/services/*
│   ├── ScheduleController      → /api/schedules/*
│   ├── AppointmentController   → /api/appointments/*
│   ├── CustomerController      → /api/customers/*
│   ├── AvailabilityController  → /api/availability/* (public)
│   └── BookingPublicController → /api/booking/* (public)
│
├── Services (Business Logic)
│   ├── AuthService
│   ├── JwtService
│   ├── BusinessService
│   ├── ServiceService
│   ├── ScheduleService
│   ├── AppointmentService
│   ├── AvailabilityService     → Calcul créneaux disponibles
│   ├── CustomerService
│   ├── NotificationService     → Gestion emails + SMS
│   ├── EmailService
│   └── SmsService (à venir)
│
├── Repositories (Data Access)
│   ├── UserRepository
│   ├── BusinessRepository
│   ├── ServiceRepository
│   ├── ScheduleRepository
│   ├── AppointmentRepository
│   ├── CustomerRepository
│   └── NotificationRepository
│
├── Models (Entities JPA)
│   ├── User
│   ├── Business
│   ├── Service
│   ├── Schedule
│   ├── ScheduleException
│   ├── Appointment
│   ├── Customer
│   ├── Notification
│   └── Subscription
│
├── DTOs (Data Transfer Objects)
│   ├── request/
│   │   ├── RegisterRequest
│   │   ├── LoginRequest
│   │   ├── CreateServiceRequest
│   │   └── BookingRequest
│   └── response/
│       ├── AuthResponse
│       ├── BusinessResponse
│       └── AppointmentResponse
│
├── Config
│   ├── SecurityConfig          → JWT + CORS
│   ├── JwtConfig
│   ├── RedisConfig
│   └── CorsConfig
│
└── Exception
    ├── GlobalExceptionHandler
    └── Custom exceptions
```

---

## 🗄️ MODÈLE DE DONNÉES

### Schéma relationnel (ERD)

```
┌──────────────────┐
│     USERS        │
├──────────────────┤
│ id (PK)          │
│ email (UK)       │
│ password_hash    │
│ first_name       │
│ last_name        │
│ phone            │
│ role             │
│ email_verified   │
│ created_at       │
│ updated_at       │
└──────────────────┘
         │
         │ 1:1
         ▼
┌──────────────────┐
│   BUSINESSES     │
├──────────────────┤
│ id (PK)          │
│ user_id (FK)     │
│ business_name    │
│ slug (UK)        │──── Exemple: "coiffeur-marie"
│ description      │
│ address          │
│ city             │
│ phone            │
│ email            │
│ category         │
│ logo_url         │
│ settings (JSON)  │
│ is_active        │
│ created_at       │
│ updated_at       │
└──────────────────┘
         │
         ├──────────────────────┬──────────────────┬──────────────────┐
         │ 1:N                  │ 1:N              │ 1:N              │ 1:N
         ▼                      ▼                  ▼                  ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────┐  ┌─────────────┐
│    SERVICES      │  │    SCHEDULES     │  │  CUSTOMERS   │  │APPOINTMENTS │
├──────────────────┤  ├──────────────────┤  ├──────────────┤  ├─────────────┤
│ id (PK)          │  │ id (PK)          │  │ id (PK)      │  │ id (PK)     │
│ business_id (FK) │  │ business_id (FK) │  │ business_id  │  │ business_id │
│ name             │  │ day_of_week      │  │ first_name   │  │ service_id  │
│ description      │  │ start_time       │  │ last_name    │  │ customer_id │
│ duration_minutes │  │ end_time         │  │ email        │  │ datetime    │
│ price            │  │ slot_duration    │  │ phone (UK)   │  │ duration    │
│ color            │  │ is_active        │  │ notes        │  │ price       │
│ is_active        │  │ created_at       │  │ total_appts  │  │ status      │
│ display_order    │  │ updated_at       │  │ last_appt_at │  │ notes       │
│ created_at       │  └──────────────────┘  │ created_at   │  │ token (UK)  │
│ updated_at       │                        │ updated_at   │  │ confirmed   │
└──────────────────┘                        └──────────────┘  │ cancelled   │
                                                               │ created_at  │
                                                               └─────────────┘
                                                                      │
                                                                      │ 1:N
                                                                      ▼
                                                            ┌──────────────────┐
                                                            │  NOTIFICATIONS   │
                                                            ├──────────────────┤
                                                            │ id (PK)          │
                                                            │ appointment_id   │
                                                            │ type             │
                                                            │ channel          │
                                                            │ recipient        │
                                                            │ content          │
                                                            │ status           │
                                                            │ sent_at          │
                                                            │ created_at       │
                                                            └──────────────────┘
```

### Tables principales

#### USERS (Utilisateurs business)
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'business' 
        CHECK (role IN ('business', 'customer', 'admin')),
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### BUSINESSES (Entreprises)
```sql
CREATE TABLE businesses (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    business_name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    description TEXT,
    address VARCHAR(500),
    city VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(255),
    category VARCHAR(50) CHECK (category IN (
        'hairdresser', 'beauty', 'health', 'sport', 'garage', 'other'
    )),
    logo_url VARCHAR(500),
    settings JSONB DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### SERVICES (Services proposés)
```sql
CREATE TABLE services (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes > 0),
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    color VARCHAR(7) DEFAULT '#3b82f6',
    is_active BOOLEAN DEFAULT TRUE,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### SCHEDULES (Horaires hebdomadaires)
```sql
CREATE TABLE schedules (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL 
        CHECK (day_of_week IN ('MON','TUE','WED','THU','FRI','SAT','SUN')),
    start_time TIME NOT NULL,
    end_time TIME NOT NULL CHECK (end_time > start_time),
    slot_duration_minutes INTEGER DEFAULT 30 CHECK (slot_duration_minutes > 0),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(business_id, day_of_week)
);
```

#### APPOINTMENTS (Rendez-vous)
```sql
CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    business_id UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    service_id UUID NOT NULL REFERENCES services(id),
    customer_id UUID NOT NULL REFERENCES customers(id),
    appointment_datetime TIMESTAMP NOT NULL,
    duration_minutes INTEGER NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending' CHECK (status IN (
        'pending', 'confirmed', 'cancelled', 'completed', 'no_show'
    )),
    notes TEXT,
    cancellation_reason TEXT,
    cancellation_token VARCHAR(64) UNIQUE,
    confirmed_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_appointments_business ON appointments(business_id);
CREATE INDEX idx_appointments_datetime ON appointments(appointment_datetime);
CREATE INDEX idx_appointments_status ON appointments(status);
```

---

## 🌐 API REST

### Authentification

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| POST | `/api/auth/register` | Inscription business | Public |
| POST | `/api/auth/login` | Connexion | Public |
| POST | `/api/auth/refresh` | Refresh token | Token |
| POST | `/api/auth/logout` | Déconnexion | Token |
| POST | `/api/auth/forgot-password` | Demande reset password | Public |
| POST | `/api/auth/reset-password` | Reset password | Public |
| GET | `/api/auth/verify-email` | Vérification email | Public |

### Business

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| GET | `/api/businesses/me` | Infos business connecté | JWT |
| PUT | `/api/businesses/me` | Modifier business | JWT |
| GET | `/api/businesses/:slug` | Infos publiques business | Public |
| PUT | `/api/businesses/me/settings` | Modifier settings | JWT |
| POST | `/api/businesses/me/logo` | Upload logo | JWT |

### Services

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| GET | `/api/services` | Liste services du business | JWT |
| POST | `/api/services` | Créer service | JWT |
| GET | `/api/services/:id` | Détails service | JWT |
| PUT | `/api/services/:id` | Modifier service | JWT |
| DELETE | `/api/services/:id` | Supprimer service | JWT |
| PUT | `/api/services/reorder` | Réorganiser ordre | JWT |

### Horaires

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| GET | `/api/schedules` | Horaires du business | JWT |
| POST | `/api/schedules` | Créer/modifier horaires | JWT |
| GET | `/api/schedules/exceptions` | Fermetures exceptionnelles | JWT |
| POST | `/api/schedules/exceptions` | Ajouter fermeture | JWT |
| DELETE | `/api/schedules/exceptions/:id` | Supprimer fermeture | JWT |

### Rendez-vous (Business)

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| GET | `/api/appointments` | Liste RDV du business | JWT |
| POST | `/api/appointments` | Créer RDV manuel | JWT |
| GET | `/api/appointments/:id` | Détails RDV | JWT |
| PUT | `/api/appointments/:id` | Modifier RDV | JWT |
| DELETE | `/api/appointments/:id` | Annuler RDV | JWT |
| PUT | `/api/appointments/:id/confirm` | Confirmer RDV | JWT |
| PUT | `/api/appointments/:id/complete` | Marquer terminé | JWT |
| PUT | `/api/appointments/:id/no-show` | Marquer no-show | JWT |
| GET | `/api/appointments/calendar` | Vue calendrier | JWT |

### Clients

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| GET | `/api/customers` | Liste clients | JWT |
| POST | `/api/customers` | Créer client | JWT |
| GET | `/api/customers/:id` | Détails client | JWT |
| PUT | `/api/customers/:id` | Modifier client | JWT |
| DELETE | `/api/customers/:id` | Supprimer client | JWT |
| GET | `/api/customers/:id/appointments` | Historique RDV client | JWT |
| GET | `/api/customers/search` | Autocomplete recherche | JWT |

### Disponibilités (Public)

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| GET | `/api/availability/dates` | Jours disponibles (mois) | Public |
| GET | `/api/availability/slots` | Créneaux horaires (jour) | Public |

Paramètres :
- `dates` : `service_id`, `year`, `month`
- `slots` : `service_id`, `date` (YYYY-MM-DD)

### Réservation publique

| Méthode | Endpoint | Description | Auth |
|---------|----------|-------------|------|
| GET | `/api/booking/:slug` | Infos business pour réservation | Public |
| GET | `/api/booking/:slug/services` | Liste services publics | Public |
| POST | `/api/booking/:slug/appointments` | Créer RDV public | Public |
| GET | `/api/booking/appointment/:token` | Détails RDV via token | Public |
| DELETE | `/api/booking/appointment/:token` | Annuler RDV via token | Public |

---

## 👤 FLUX UTILISATEURS

### 1. Inscription business

```
User (navigateur)
    │
    │ 1. Remplit formulaire inscription
    │    (email, password, nom, prénom, business_name)
    ▼
Frontend (Angular)
    │
    │ 2. Validation côté client
    │ 3. POST /api/auth/register
    ▼
Backend (Spring Boot)
    │
    │ 4. Vérification email unique
    │ 5. Hash password (BCrypt)
    │ 6. INSERT users + businesses (génère slug unique)
    │ 7. Génère JWT + email verification token
    │ 8. Envoie email vérification (Gmail SMTP)
    │ 9. Return 201 + JWT
    ▼
Frontend
    │
    │ 10. Stocke JWT dans localStorage
    │ 11. Redirect /dashboard
    ▼
User clique lien email
    │
    │ 12. GET /api/auth/verify-email?token=xxx
    ▼
Backend
    │
    │ 13. UPDATE email_verified = true
    │ 14. Return 200
    ▼
Frontend
    │
    │ 15. Affiche "Email vérifié ✓"
```

### 2. Configuration initiale (horaires + services)

```
Business Dashboard
    │
    │ 1. Navigate to /horaires
    │ 2. GET /api/schedules (vide si premier login)
    ▼
Wizard Configuration
    │
    │ 3. User définit horaires hebdo
    │    Exemple: Lun-Ven 9h-18h, Sam 9h-13h, Dim fermé
    │ 4. POST /api/schedules
    ▼
Backend
    │
    │ 5. INSERT INTO schedules (multiple rows)
    │    - MON: 09:00-18:00, slot 30min
    │    - TUE: 09:00-18:00, slot 30min
    │    - ...
    │    - SAT: 09:00-13:00, slot 30min
    │ 6. Return 201
    ▼
Frontend
    │
    │ 7. Navigate to /services
    │ 8. User crée service
    │    Exemple: "Coupe homme - 30min - 25€"
    │ 9. POST /api/services
    ▼
Backend
    │
    │ 10. INSERT INTO services
    │ 11. Return 201
    ▼
Frontend
    │
    │ 12. Affiche lien réservation unique
    │     → reservez.app/coiffeur-marie
```

### 3. Réservation client (complet avec vérifications)

```
Client (web)
    │
    │ 1. Visite reservez.app/coiffeur-marie
    │ 2. GET /api/booking/coiffeur-marie
    ▼
Backend
    │
    │ 3. SELECT business WHERE slug = 'coiffeur-marie'
    │ 4. SELECT services WHERE business_id = X AND is_active = true
    │ 5. Return {business, services}
    ▼
Frontend (page publique)
    │
    │ 6. Affiche page avec liste services
    │ 7. Client sélectionne "Coupe femme - 45min - 35€"
    │ 8. GET /api/availability/dates?service_id=X&month=2024-11
    ▼
Backend (AvailabilityService)
    │
    │ 9. SELECT schedules (horaires hebdo)
    │ 10. SELECT appointments ce mois (status confirmed/pending)
    │ 11. SELECT schedule_exceptions (fermetures)
    │ 12. Calcul jours disponibles:
    │     - Enlève jours sans horaires (ex: dimanche)
    │     - Enlève jours de fermeture exceptionnelle
    │     - Enlève jours où tous créneaux occupés
    │ 13. Return {availableDates: [25, 26, 27, 28, 29, 30]}
    ▼
Frontend
    │
    │ 14. Affiche calendrier avec jours en surbrillance
    │ 15. Client sélectionne 25 novembre
    │ 16. GET /api/availability/slots?service_id=X&date=2024-11-25
    ▼
Backend (AvailabilityService)
    │
    │ 17. SELECT schedule WHERE day_of_week = 'MON'
    │     Result: {start: 09:00, end: 18:00, slot_duration: 30}
    │ 18. Génère tous les créneaux possibles de 30min:
    │     [09:00, 09:30, 10:00, 10:30, 11:00, 11:30, ...]
    │ 19. SELECT appointments ce jour avec FOR UPDATE
    │ 20. Enlève créneaux occupés
    │ 21. Enlève créneaux insuffisants pour durée service (45min)
    │ 22. Return {slots: ["09:00", "09:30", "10:00", "11:30", ...]}
    ▼
Frontend
    │
    │ 23. Affiche créneaux disponibles (indisponibles grisés)
    │ 24. Client sélectionne 10:00
    │ 25. Client remplit formulaire:
    │     - Prénom: Marie
    │     - Nom: Dupont
    │     - Email: marie@example.com
    │     - Téléphone: 0612345678
    │     - Notes: "Première visite"
    │ 26. POST /api/booking/coiffeur-marie/appointments
    ▼
Backend (BookingService)
    │
    │ 27. BEGIN TRANSACTION
    │ 28. Double-check disponibilité avec SELECT FOR UPDATE
    │     → Évite double-booking concurrent
    │ 29. SELECT customer WHERE phone = '0612345678'
    │     → Si existe: récupère customer_id
    │     → Si pas: INSERT INTO customers, récupère nouveau id
    │ 30. INSERT INTO appointments:
    │     - business_id, service_id, customer_id
    │     - datetime: 2024-11-25 10:00
    │     - duration: 45, price: 35
    │     - status: 'confirmed'
    │     - cancellation_token: généré (UUID)
    │ 31. UPDATE customers SET total_appointments++
    │ 32. COMMIT TRANSACTION
    │ 33. Queue notification email (async)
    │ 34. INSERT INTO notifications (type: confirmation, channel: email)
    ▼
NotificationService (async)
    │
    │ 35. Envoie email confirmation via Gmail SMTP:
    │     - Récapitulatif RDV (date, heure, service, prix)
    │     - Lien annulation: /booking/cancel/:token
    │     - Fichier .ics pour calendrier
    │ 36. UPDATE notification status = 'sent'
    ▼
Backend → Frontend
    │
    │ 37. Return 201 Created {appointment}
    ▼
Frontend
    │
    │ 38. Affiche page "RDV confirmé ✓"
    │ 39. Affiche récapitulatif complet
    │ 40. Bouton "Ajouter au calendrier"
```

---

## 🔐 SÉCURITÉ

### Authentification JWT

```
Client                    Backend
   │                         │
   │  1. POST /api/auth/login │
   │  {email, password}      │
   │─────────────────────────>│
   │                         │ 2. Vérif credentials BCrypt
   │                         │ 3. Génère JWT (expire 1h)
   │                         │ 4. Génère Refresh Token (expire 7j)
   │  5. Return tokens       │
   │<─────────────────────────│
   │  {accessToken, refresh} │
   │                         │
   │  6. Stocke dans        │
   │     localStorage        │
   │                         │
   │  7. GET /api/services   │
   │  Header: Authorization  │
   │  Bearer <JWT>           │
   │─────────────────────────>│
   │                         │ 8. Valide JWT
   │                         │ 9. Extrait user_id du token
   │                         │ 10. Return données
   │<─────────────────────────│
```

### Structure JWT

```json
{
  "sub": "user-uuid",
  "email": "coiffeur@example.com",
  "role": "business",
  "iat": 1700000000,
  "exp": 1700003600
}
```

### Endpoints publics vs protégés

**Public (pas de JWT requis) :**
- Landing page, pricing, features
- Page réservation : `/api/booking/:slug`
- Disponibilités : `/api/availability/*`
- Création RDV : `POST /api/booking/:slug/appointments`
- Auth : `POST /api/auth/login`, `POST /api/auth/register`

**Protégé (JWT requis) :**
- Tout sous `/api/businesses/*`
- Tout sous `/api/services/*`
- Tout sous `/api/schedules/*`
- Tout sous `/api/appointments/*` (côté business)
- Tout sous `/api/customers/*`

---

## 🚀 DÉPLOIEMENT

### Environnements

| Environnement | Frontend | Backend | Database |
|---------------|----------|---------|----------|
| **Dev local** | localhost:4200 | localhost:8080 | Docker (5433) |
| **Staging** | staging.reservez.app | api-staging.reservez.app | Supabase (staging) |
| **Production** | reservez.app | api.reservez.app | Supabase (prod) |

### Pipeline CI/CD (GitHub Actions)

```yaml
# .github/workflows/deploy.yml

Frontend:
  1. Checkout code
  2. Setup Node.js 20
  3. npm install
  4. npm run build:ssr (Angular SSR)
  5. Deploy to Vercel

Backend:
  1. Checkout code
  2. Setup Java 21
  3. mvn clean package
  4. Build Docker image
  5. Push to Railway
  6. Run migrations (Flyway)
```

### Variables d'environnement

**Frontend (.env) :**
```
VITE_API_URL=https://api.reservez.app
VITE_ENVIRONMENT=production
```

**Backend (application-prod.yml) :**
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
  
jwt:
  secret: ${JWT_SECRET}
  expiration: 3600000

email:
  smtp:
    username: ${SMTP_USERNAME}
    password: ${SMTP_PASSWORD}
```

---

## 📁 STRUCTURE DU PROJET

```
booking-platform/
├── frontend/                       # Angular 18 + SSR
│   ├── src/
│   │   ├── app/
│   │   │   ├── core/
│   │   │   │   ├── auth/
│   │   │   │   │   ├── guards/
│   │   │   │   │   ├── interceptors/
│   │   │   │   │   └── services/
│   │   │   │   ├── models/
│   │   │   │   └── services/
│   │   │   ├── shared/
│   │   │   │   ├── components/
│   │   │   │   ├── pipes/
│   │   │   │   └── directives/
│   │   │   ├── features/
│   │   │   │   ├── landing/          # SSR
│   │   │   │   ├── auth/             # CSR
│   │   │   │   ├── dashboard/        # CSR
│   │   │   │   ├── calendar/         # CSR
│   │   │   │   ├── appointments/     # CSR
│   │   │   │   ├── services/         # CSR
│   │   │   │   ├── schedule/         # CSR
│   │   │   │   ├── customers/        # CSR
│   │   │   │   ├── analytics/        # CSR
│   │   │   │   ├── settings/         # CSR
│   │   │   │   └── booking-public/   # SSR
│   │   │   ├── app.routes.ts
│   │   │   └── app.config.ts
│   │   ├── environments/
│   │   ├── assets/
│   │   └── styles/
│   ├── angular.json
│   ├── package.json
│   ├── tailwind.config.js
│   ├── tsconfig.json
│   └── vercel.json
│
├── backend/                        # Spring Boot 3.4
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/booking/api/
│   │   │   │   ├── config/
│   │   │   │   │   ├── SecurityConfig.java
│   │   │   │   │   ├── JwtConfig.java
│   │   │   │   │   ├── RedisConfig.java
│   │   │   │   │   └── CorsConfig.java
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AuthController.java
│   │   │   │   │   ├── BusinessController.java
│   │   │   │   │   ├── ServiceController.java
│   │   │   │   │   ├── ScheduleController.java
│   │   │   │   │   ├── AppointmentController.java
│   │   │   │   │   ├── CustomerController.java
│   │   │   │   │   ├── AvailabilityController.java
│   │   │   │   │   └── BookingPublicController.java
│   │   │   │   ├── service/
│   │   │   │   │   ├── AuthService.java
│   │   │   │   │   ├── JwtService.java
│   │   │   │   │   ├── BusinessService.java
│   │   │   │   │   ├── ServiceService.java
│   │   │   │   │   ├── ScheduleService.java
│   │   │   │   │   ├── AppointmentService.java
│   │   │   │   │   ├── AvailabilityService.java
│   │   │   │   │   ├── CustomerService.java
│   │   │   │   │   ├── NotificationService.java
│   │   │   │   │   ├── EmailService.java
│   │   │   │   │   └── SmsService.java
│   │   │   │   ├── repository/
│   │   │   │   │   ├── UserRepository.java
│   │   │   │   │   ├── BusinessRepository.java
│   │   │   │   │   ├── ServiceRepository.java
│   │   │   │   │   ├── ScheduleRepository.java
│   │   │   │   │   ├── AppointmentRepository.java
│   │   │   │   │   └── CustomerRepository.java
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Business.java
│   │   │   │   │   ├── Service.java
│   │   │   │   │   ├── Schedule.java
│   │   │   │   │   ├── ScheduleException.java
│   │   │   │   │   ├── Appointment.java
│   │   │   │   │   ├── Customer.java
│   │   │   │   │   └── Notification.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── request/
│   │   │   │   │   └── response/
│   │   │   │   ├── exception/
│   │   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   │   └── CustomExceptions.java
│   │   │   │   └── util/
│   │   │   │       ├── DateTimeUtil.java
│   │   │   │       └── SlotCalculator.java
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       ├── application-dev.yml
│   │   │       ├── application-prod.yml
│   │   │       └── db/migration/
│   │   │           ├── V1__init_schema.sql
│   │   │           ├── V2__add_notifications.sql
│   │   │           └── V3__add_subscriptions.sql
│   │   └── test/
│   ├── pom.xml
│   ├── Dockerfile
│   └── .dockerignore
│
├── database/
│   ├── migrations/                 # Backups Flyway
│   │   └── V1__init_schema.sql
│   └── seeds/                      # Données de dev/test
│       └── dev_data.sql
│
├── docs/
│   ├── ARCHITECTURE.md             # Ce fichier
│   ├── API_SPECIFICATION.md
│   ├── DATABASE_SCHEMA.md
│   ├── DIAGRAMMES_ARCHITECTURE.md
│   └── SETUP_GUIDE.md
│
├── .github/
│   └── workflows/
│       └── ci.yml
│
├── .gitignore
├── docker-compose.yml
└── README.md
```

---

## 📊 MÉTRIQUES & MONITORING

### KPIs à suivre

**Business :**
- Nombre de business inscrits
- Taux de conversion freemium → payant
- MRR (Monthly Recurring Revenue)
- Churn rate
- Nombre de RDV créés/mois

**Technique :**
- Uptime API (> 99.5%)
- Latence P95 endpoints (< 500ms)
- Taux d'erreur (< 1%)
- Temps de réponse base de données
- Utilisation ressources (CPU, RAM, DB)

### Outils

- **Monitoring** : Railway metrics + Vercel Analytics
- **Logs** : Spring Boot Actuator + Logback
- **Errors** : Sentry (à venir)
- **Analytics** : Plausible Analytics (privacy-friendly)

---

## 🔄 ÉVOLUTIONS FUTURES (Roadmap)

### Phase 1 (MVP - Mois 1-3)
- ✅ Auth + Dashboard basique
- ✅ Gestion services & horaires
- ✅ Réservation publique
- ✅ Notifications email
- ✅ Calendrier business

### Phase 2 (Mois 4-6)
- 🔲 Notifications SMS (Twilio)
- 🔲 Google Calendar sync
- 🔲 Analytics avancés
- 🔲 Export CSV rendez-vous
- 🔲 Widget embeddable (iframe)

### Phase 3 (Mois 7-12)
- 🔲 Application mobile (React Native)
- 🔲 Gestion employés multi-utilisateurs
- 🔲 Paiement en ligne (Stripe)
- 🔲 Programme de fidélité
- 🔲 Marketplace de services

---

## 📞 SUPPORT & CONTACT

**Développeur :** Alexis  
**Email :** alexis.cattaruzza@gmail.com 
**GitHub :** https://github.com/alexis-cattaruzza
**Localisation :** Genève, Suisse

---

**Version du document :** 1.0  
**Dernière mise à jour :** 25 novembre 2024  
**Status :** ✅ En développement actif