# Backend - Booking Platform API

API REST pour la plateforme de réservation.

## 🚀 Démarrage rapide

### Prérequis
- Java 21 LTS
- Maven 3.9.x
- PostgreSQL 16+

### Lancement

```bash
# Avec Maven
mvn spring-boot:run

# Ou avec le wrapper Maven
./mvnw spring-boot:run
```

L'API sera disponible sur : http://localhost:8080

### Endpoints disponibles

- **Health check** : http://localhost:8080/actuator/health
- **API docs** : http://localhost:8080/swagger-ui.html (à venir)

## 🗄️ Base de données

### Configuration locale

La configuration par défaut se connecte à :
- Host : localhost:5432
- Database : booking_db
- User : postgres
- Password : postgres

### Migrations Flyway

Les migrations sont automatiquement exécutées au démarrage de l'application.

Fichiers de migration : `src/main/resources/db/migration/`

## 🛠️ Développement

### Structure du projet

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/booking/api/
│   │   │   ├── config/          # Configuration Spring
│   │   │   ├── controller/      # Controllers REST
│   │   │   ├── service/         # Services métier
│   │   │   ├── repository/      # Repositories JPA
│   │   │   ├── model/           # Entités JPA
│   │   │   ├── dto/             # DTOs
│   │   │   └── exception/       # Gestion erreurs
│   │   └── resources/
│   │       ├── application.yml  # Config principale
│   │       └── db/migration/    # Migrations Flyway
│   └── test/
└── pom.xml
```

### Tests

```bash
# Lancer tous les tests
mvn test

# Lancer un test spécifique
mvn test -Dtest=ClasseTest
```

## 📦 Build

```bash
# Build sans tests
mvn clean package -DskipTests

# Build avec tests
mvn clean package
```

Le JAR sera généré dans : `target/booking-api-0.0.1-SNAPSHOT.jar`

## 🐳 Docker

```bash
# Build de l'image
docker build -t booking-api .

# Run
docker run -p 8080:8080 booking-api
```
