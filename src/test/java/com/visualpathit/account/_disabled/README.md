# Tests Désactivés (Disabled Tests)

Ces tests d'intégration avancés sont **désactivés par défaut** pour faciliter le training DevOps/DevSecOps/GitOps.

## 📂 Tests Disponibles Ici

| Fichier | Tests | Description |
|---------|-------|-------------|
| `UserRepositoryIntegrationTest.java` | 13 | Tests JPA avec H2 en mémoire |
| `TimelineControllerIntegrationTest.java` | 12 | Tests contrôleur avec MockMvc |
| `AuthenticationSecurityTest.java` | 15 | Tests sécurité OWASP (A01, A02, A07) |
| `InjectionSecurityTest.java` | 14 | Tests anti-injection (SQL, XSS, etc.) |
| `UserJourneyE2ETest.java` | 10 | Tests end-to-end complets |

**Total** : 64 tests d'intégration avancés

## ⚠️ Pourquoi Désactivés ?

Ces tests nécessitent :
- Configuration Spring Boot complète (`@SpringBootTest`)
- Gestion Flyway/migrations de base de données
- Configuration H2 en conflit avec MySQL
- Temps d'exécution plus long (5-10 minutes)

**Pour du training DevOps**, les ~75 tests unitaires actifs suffisent amplement !

## 🔄 Comment Réactiver ?

### Option 1 : Tout réactiver

```bash
# Déplacer tous les tests vers leur emplacement d'origine
mv src/test/java/com/visualpathit/account/_disabled/repositoryTest src/test/java/com/visualpathit/account/
mv src/test/java/com/visualpathit/account/_disabled/securityTest src/test/java/com/visualpathit/account/
mv src/test/java/com/visualpathit/account/_disabled/e2eTest src/test/java/com/visualpathit/account/
mv src/test/java/com/visualpathit/account/_disabled/TimelineControllerIntegrationTest.java src/test/java/com/visualpathit/account/controllerTest/

# Exécuter TOUS les tests
mvn test
```

### Option 2 : Réactiver seulement les tests de sécurité

```bash
# Réactiver uniquement les tests DevSecOps (OWASP Top 10)
mv src/test/java/com/visualpathit/account/_disabled/securityTest src/test/java/com/visualpathit/account/

# Exécuter
mvn test
```

### Option 3 : Exécuter sans réactiver (tests ignorés)

```bash
# Les tests dans _disabled/ ne seront PAS exécutés par Maven
# Ils restent dans le code source pour référence/documentation
```

## 📋 Prérequis pour Réactiver

Si vous réactivez ces tests, il faudra :

1. **Désactiver Flyway dans les tests** :
```java
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    FlywayAutoConfiguration.class  // ← Ajouter
})
public class TestApplication {
    // ...
}
```

2. **OU** Configurer Flyway pour H2 :
```yaml
# src/test/resources/application-test.yml
spring:
  flyway:
    enabled: false  # Désactiver pour les tests
```

3. **OU** Utiliser `@AutoConfigureTestDatabase` :
```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserRepositoryIntegrationTest {
    // Force H2 au lieu de MySQL
}
```

## ✅ Tests Actifs (Toujours Exécutés)

Les tests suivants restent actifs et s'exécutent à chaque `mvn test` :

- ✅ **PostLikeServiceTest** (18 tests) - Service de likes
- ✅ **SecurityServiceImplTest** (9 tests) - Authentification
- ✅ **PostServiceImplTest** (15 tests) - Service de posts
- ✅ **UserServiceImplTest** (10 tests) - Service utilisateur
- ✅ **UserValidatorTest** (23 tests) - Validation formulaires
- ✅ **Legacy tests** (9 tests) - Tests historiques

**Total actif** : ~75-85 tests rapides (<2 minutes)

---

**Note** : Ces tests désactivés sont conservés dans le code source pour :
- Documentation des bonnes pratiques de test
- Référence pour les formations avancées
- Possibilité de réactivation future si besoin
