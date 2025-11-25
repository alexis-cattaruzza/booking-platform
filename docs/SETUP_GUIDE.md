# 🛠️ Guide d'installation développeur

## Prérequis

### Versions requises
- Node.js 20.x LTS
- npm 10.x
- Java 21 LTS
- Maven 3.9.x
- PostgreSQL 16+
- Git

### Installation Node.js
Télécharge depuis : https://nodejs.org/

### Installation Java 21
Télécharge Amazon Corretto 21 : https://docs.aws.amazon.com/corretto/

### Installation PostgreSQL
Télécharge depuis : https://www.postgresql.org/download/

## Configuration

### 1. Base de données locale

```bash
# Crée la base de données
createdb booking_db

# Ou via psql
psql -U postgres
CREATE DATABASE booking_db;
```

### 2. Configuration Backend

Copie `application-example.yml` vers `application-dev.yml` :

```bash
cd backend/src/main/resources
cp application-example.yml application-dev.yml
```

Édite `application-dev.yml` avec tes credentials PostgreSQL.

### 3. Configuration Frontend

```bash
cd frontend
npm install
```

Copie `src/environments/environment.example.ts` vers `environment.development.ts`.

## Lancement du projet

### Terminal 1 : Backend
```bash
cd backend
mvn spring-boot:run
```

### Terminal 2 : Frontend
```bash
cd frontend
npm start
```

Accède à l'application : http://localhost:4200

## Commandes utiles

### Frontend
```bash
npm start              # Démarre le dev server
npm run build          # Build production
npm test               # Lance les tests
ng generate component  # Génère un composant
```

### Backend
```bash
mvn spring-boot:run        # Lance l'application
mvn test                   # Lance les tests
mvn clean install          # Build le projet
```

## Troubleshooting

### Port 8080 déjà utilisé
Change le port dans `application.yml` :
```yaml
server:
  port: 8081
```

### Erreur connexion DB
Vérifie que PostgreSQL est lancé :
```bash
# Windows
net start postgresql-x64-16

# Mac
brew services start postgresql@16

# Linux
sudo systemctl start postgresql
```
