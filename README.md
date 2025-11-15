# Facelink - Professional Social Network

[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![Spring](https://img.shields.io/badge/Spring-6.0-green)](https://spring.io/)
[![Flyway](https://img.shields.io/badge/Flyway-9.22-blue)](https://flywaydb.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)

## Prerequisites

- JDK 17 or 21
- Maven 3.9+
- MySQL 8.0+
- Docker & Docker Compose (recommended)

## Technologies

- **Backend Framework**: Spring MVC 6.0
- **Security**: Spring Security 6.1
- **Data Access**: Spring Data JPA 3.1, Hibernate 7.0
- **Database**: MySQL 8.0
- **Database Migrations**: Flyway 9.22 ✨
- **Build Tool**: Maven 3.9
- **View Technology**: JSP with JSTL
- **Application Server**: Tomcat 10.1
- **Caching**: Memcached (alpine)
- **Message Queue**: RabbitMQ 3 with Management UI
- **Search Engine**: Elasticsearch 7.17.18

## Database Migration avec Flyway 🚀

Ce projet utilise **Flyway** pour gérer les migrations de base de données de manière professionnelle et reproductible.

### Avantages de Flyway

✅ **Production-ready** : Standard industriel pour les migrations de bases de données
✅ **Historique versionné** : Toutes les modifications sont tracées et versionnées
✅ **Reproductible** : Même schéma sur dev/staging/production
✅ **Audit complet** : Table `flyway_schema_history` pour le suivi
✅ **CI/CD ready** : Migrations appliquées automatiquement au déploiement

### Fichiers de Migration

Les migrations sont situées dans `src/main/resources/db/migration/` :

- **V1__initial_schema.sql** - Tables `user`, `role`, `user_role` + rôles par défaut
- **V2__create_posts_table.sql** - Table `posts` pour la timeline
- **V3__create_post_likes_table.sql** - Table `post_likes` pour les likes

### Configuration Flyway

La configuration Flyway se trouve dans `application.properties` :

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.flyway.validate-on-migrate=true
```

**Important** : Hibernate est configuré avec `ddl-auto=none` dans `appconfig-data.xml`, car Flyway gère maintenant le schéma.

### Ajouter une Nouvelle Migration

Pour ajouter une nouvelle fonctionnalité nécessitant des modifications de base de données :

1. Créer un fichier `VX__description.sql` dans `src/main/resources/db/migration/`
2. Utiliser un numéro de version croissant (V5, V6, etc.)
3. Nommer en snake_case avec underscores (ex: `V5__add_comments_table.sql`)
4. Redémarrer l'application → Flyway applique automatiquement la nouvelle migration

**Exemple** :

```sql
-- V5__add_comments_table.sql
CREATE TABLE IF NOT EXISTS post_comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content VARCHAR(500) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
);
```

### Vérifier l'Historique des Migrations

Pour voir l'historique complet des migrations appliquées :

```bash
docker compose exec mysql mysql -uroot -pvpropassword accounts \
  -e "SELECT version, description, installed_on, success FROM flyway_schema_history;"
```

### Règles Importantes

⚠️ **Les migrations Flyway sont IMMUABLES** : Ne jamais modifier une migration déjà appliquée (V1, V2, etc.)
⚠️ **Corrections** : En cas d'erreur, créer une nouvelle migration de correction (V5, V6, etc.)
⚠️ **Tests** : Toujours tester les migrations sur une copie de la base avant production

## Démarrage Rapide

### Avec Docker Compose (Recommandé)

```bash
# Construire et démarrer tous les services
docker compose up -d

# Voir les logs de l'application
docker compose logs -f app

# Vérifier que les migrations Flyway ont été appliquées
docker compose logs app | grep -i flyway
```

**Sortie attendue** :

```
Flyway: Migrating schema `accounts` to version "1 - initial schema"
Flyway: Migrating schema `accounts` to version "2 - create posts table"
Flyway: Migrating schema `accounts` to version "3 - create post likes table"
Flyway: Successfully applied 3 migrations to schema `accounts`
```

### Fresh Start (Base de Données Vide)

Pour repartir de zéro avec une base de données propre :

```bash
# Arrêter et supprimer tous les conteneurs et volumes
docker compose down -v

# Rebuilder l'application (si modifications du code)
docker compose build --no-cache app

# Redémarrer
docker compose up -d

# Flyway créera automatiquement toutes les tables
```

### Accès à l'Application

- **URL** : http://localhost:8080
- **Admin** :
  - Username: `admin`
  - Password: `admin123`

## Utilisateur Admin

Un utilisateur admin est automatiquement créé au premier démarrage par `DataInitializer.java` :

- **Username** : `admin`
- **Email** : `admin@facelink.com`
- **Password** : `admin123`
- **Roles** : `ROLE_USER`, `ROLE_ADMIN`

L'admin a accès à un **panneau d'administration** avec des boutons de vérification système :
- ✅ **Tous les Utilisateurs** - Liste tous les utilisateurs inscrits
- ✅ **RabbitMQ** - Vérifie la connexion au service RabbitMQ
- ✅ **Elasticsearch** - Indexe les utilisateurs dans Elasticsearch

Ces boutons sont visibles **uniquement pour les utilisateurs avec ROLE_ADMIN**.

## Services Backend

L'application utilise plusieurs services Docker :

| Service | Port | Accès | Description |
|---------|------|-------|-------------|
| **MySQL** | 3306 | `mysql://localhost:3306` | Base de données principale |
| **RabbitMQ** | 5672, 15672 | http://localhost:15672 | Message queue + Management UI (guest/guest) |
| **Memcached** | 11211 | `localhost:11211` | Cache en mémoire |
| **Elasticsearch** | 9200, 9300 | http://localhost:9200 | Moteur de recherche |
| **App (Tomcat)** | 8080 | http://localhost:8080 | Application web |

### Vérifier les Services

```bash
# Voir l'état de tous les services
docker compose ps

# Accéder à RabbitMQ Management UI
open http://localhost:15672  # guest/guest

# Tester Elasticsearch
curl http://localhost:9200
```

## Health Check Endpoints (Kubernetes Ready) 🏥

L'application expose des endpoints de santé pour l'orchestration Kubernetes et le monitoring.

### Endpoints Disponibles

| Endpoint | Type | Description | Retour |
|----------|------|-------------|--------|
| `GET /health` | **Liveness** | Vérifie que l'application est vivante | `{"status": "UP"}` |
| `GET /ready` | **Readiness** | Vérifie que l'app est prête (MySQL OK) | `{"status": "UP", "database": "UP"}` |
| `GET /version` | **Info** | Informations de version et build | `{"app": "facelink", "version": "1.0.0", "buildTime": "..."}` |

### Caractéristiques

✅ **Accès public** : Aucune authentification requise (autorisé dans Spring Security)
✅ **Format JSON** : Retours en JSON pour parsing automatique
✅ **Rapide** : `/health` répond en <10ms, `/ready` en <100ms
✅ **Production-ready** : Timeout de 2s sur les checks de base de données

### Tester les Endpoints

```bash
# Liveness probe - Application vivante ?
curl http://localhost:8080/health
# {"status":"UP"}

# Readiness probe - Application prête à servir du trafic ?
curl http://localhost:8080/ready
# {"status":"UP","database":"UP"}

# Version - Quelle version est déployée ?
curl http://localhost:8080/version
# {"app":"facelink","version":"1.0.0","buildTime":"2025-11-15T10:30:00Z"}
```

### Configuration Kubernetes

Exemple de configuration de probes dans vos manifests K8s :

```yaml
apiVersion: v1
kind: Pod
metadata:
  name: facelink
spec:
  containers:
  - name: facelink
    image: facelink:1.0.0
    ports:
    - containerPort: 8080

    # Liveness probe - Redémarre le pod si non responsive
    livenessProbe:
      httpGet:
        path: /health
        port: 8080
      initialDelaySeconds: 30
      periodSeconds: 10
      timeoutSeconds: 5
      failureThreshold: 3

    # Readiness probe - Retire du service si non prêt
    readinessProbe:
      httpGet:
        path: /ready
        port: 8080
      initialDelaySeconds: 10
      periodSeconds: 5
      timeoutSeconds: 3
      failureThreshold: 3

    # Startup probe - Donne du temps au démarrage (Flyway migrations)
    startupProbe:
      httpGet:
        path: /ready
        port: 8080
      initialDelaySeconds: 0
      periodSeconds: 10
      timeoutSeconds: 3
      failureThreshold: 30  # 30 * 10s = 5 minutes max pour démarrer
```

### Codes de Retour HTTP

| Endpoint | Success | Failure | Signification |
|----------|---------|---------|---------------|
| `/health` | `200 OK` | - | Application process vivant |
| `/ready` | `200 OK` | `503 Service Unavailable` | MySQL accessible ou non |
| `/version` | `200 OK` | - | Toujours disponible |

### Utilisation DevOps

**GitOps (ArgoCD/Flux)** : Les probes sont automatiquement utilisées pour déterminer la santé du déploiement

**Monitoring (Prometheus)** : Les endpoints peuvent être scrapés pour créer des métriques :
```promql
probe_success{endpoint="/ready"} == 0  # Alerte si readiness échoue
```

**CI/CD** : Vérifier la santé après déploiement :
```bash
# Attendre que l'app soit prête
until curl -f http://localhost:8080/ready; do sleep 5; done
echo "Application is ready!"
```

## Structure du Projet

```
facelink/
├── src/main/
│   ├── java/com/visualpathit/account/
│   │   ├── config/          # Configuration Spring
│   │   ├── controller/      # Contrôleurs MVC
│   │   ├── model/           # Entités JPA
│   │   ├── repository/      # Repositories Spring Data
│   │   ├── service/         # Services métier
│   │   └── validator/       # Validateurs
│   ├── resources/
│   │   ├── db/migration/    # Migrations Flyway ✨
│   │   │   ├── V1__initial_schema.sql
│   │   │   ├── V2__create_posts_table.sql
│   │   │   └── V3__create_post_likes_table.sql
│   │   └── application.properties
│   └── webapp/
│       ├── WEB-INF/
│       │   ├── views/       # JSP views
│       │   └── appconfig-*.xml
│       └── resources/       # CSS, JS, Images
├── pom.xml
└── docker-compose.yml
```

## Build & Tests

```bash
# Compiler le projet
mvn clean compile

# Exécuter les tests
mvn test

# Générer le WAR
mvn clean package

# Générer le rapport de couverture JaCoCo
mvn jacoco:report
```

## Contribution

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Si votre feature nécessite des modifications de base de données, créer une migration Flyway
4. Commit vos changements (`git commit -m 'feat: Add amazing feature'`)
5. Push vers la branche (`git push origin feature/AmazingFeature`)
6. Ouvrir une Pull Request

## License

Ce projet est sous licence MIT - voir le fichier LICENSE pour plus de détails.

---

**Note** : Ce projet a été migré de `hibernate.hbm2ddl.auto=update` vers Flyway pour une gestion professionnelle des migrations de base de données. Cette approche est recommandée pour les environnements de production.
