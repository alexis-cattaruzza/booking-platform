# 🗓️ Booking Platform

> Plateforme de réservation simplifiée pour PME locales  
> Alternative française à Calendly

## 🚀 Quick Start

### Prérequis
- Node.js 20.x LTS
- Java 21 LTS
- Maven 3.9.x
- PostgreSQL 16+

### Installation

```bash
# Cloner le repository
git clone <your-repo-url>
cd booking-platform

# Frontend
cd frontend
npm install
npm start
# → http://localhost:4200

# Backend
cd backend
mvn spring-boot:run
# → http://localhost:8080
```

## 📁 Structure du projet

```
booking-platform/
├── frontend/          # Application Angular 18
├── backend/           # API Spring Boot 3
├── database/          # Scripts SQL et migrations
├── docs/              # Documentation complète
└── .github/           # CI/CD workflows
```

## 🛠️ Stack technique

**Frontend**
- Angular 18
- TypeScript 5
- Tailwind CSS 3
- RxJS 7

**Backend**
- Java 21
- Spring Boot 3.2
- Spring Security 6
- PostgreSQL 16

**Infrastructure**
- Docker & Docker Compose
- Vercel (Frontend)
- Railway (Backend)

## 📖 Documentation

Consulte le dossier [`docs/`](./docs/) pour la documentation complète :
- [Architecture](./docs/ARCHITECTURE.md)
- [API Specification](./docs/API_SPECIFICATION.md)
- [Database Schema](./docs/DATABASE_SCHEMA.md)
- [Setup Guide](./docs/SETUP_GUIDE.md)

## 🤝 Contribution

Ce projet est développé par Alexis pour le marché francophone.

## 📝 License

Propriétaire - Tous droits réservés
