# 🧪 Guide Complet des Tests - Facelink (Formation DevOps/DevSecOps/GitOps)

## 📋 Table des matières

1. [Introduction](#introduction)
2. [Architecture des Tests](#architecture-des-tests)
3. [Types de Tests Implémentés](#types-de-tests-implémentés)
4. [Exécution des Tests](#exécution-des-tests)
5. [Couverture de Code](#couverture-de-code)
6. [Quality Gates CI/CD](#quality-gates-cicd)
7. [Tests de Sécurité (DevSecOps)](#tests-de-sécurité-devsecops)
8. [Bonnes Pratiques](#bonnes-pratiques)
9. [Dépannage](#dépannage)

---

## 🎯 Introduction

Ce guide est conçu pour la **formation DevOps/DevSecOps/GitOps**. Il couvre tous les aspects des tests automatisés dans un pipeline CI/CD moderne.

### Pourquoi les tests sont essentiels en CI/CD ?

```
Code → Build → Tests ✅ → Quality Gates → Deploy ✅
                 ↓ ❌
            Pipeline STOPPED
```

**Sans tests :**
- ❌ Déploiements risqués
- ❌ Bugs en production
- ❌ Pas de métriques qualité
- ❌ Pipeline inutile

**Avec tests :**
- ✅ Déploiements sûrs
- ✅ Détection précoce des bugs (Shift-Left)
- ✅ Métriques qualité (SonarQube)
- ✅ Conformité DevSecOps

---

## 🏗️ Architecture des Tests

### Pyramide des Tests

```
           /\
          /E2E\         ← Tests End-to-End (UserJourneyE2ETest)
         /------\
        /Security\      ← Tests de Sécurité (DevSecOps)
       /----------\
      /Integration\     ← Tests d'Intégration (Repositories, Controllers)
     /--------------\
    /  Unit Tests   \   ← Tests Unitaires (Services, Validators)
   /------------------\
```

### Structure des Tests

```
src/test/java/com/visualpathit/account/
├── controllerTest/
│   ├── UserControllerTest.java                 (Existant - MockMvc)
│   └── TimelineControllerIntegrationTest.java  (Nouveau - Full Spring Context)
│
├── serviceTest/
│   ├── UserServiceImplTest.java                (Nouveau - Unit)
│   ├── SecurityServiceImplTest.java            (Nouveau - Unit)
│   ├── PostServiceImplTest.java                (Nouveau - Unit)
│   └── PostLikeServiceTest.java                (Nouveau - Unit)
│
├── repositoryTest/
│   └── UserRepositoryIntegrationTest.java      (Nouveau - JPA Integration)
│
├── validatorTest/
│   └── UserValidatorTest.java                  (Nouveau - Validation Logic)
│
├── securityTest/
│   ├── AuthenticationSecurityTest.java         (Nouveau - OWASP A01, A02, A07)
│   └── InjectionSecurityTest.java              (Nouveau - OWASP A03)
│
└── e2eTest/
    └── UserJourneyE2ETest.java                 (Nouveau - Complete Workflows)
```

---

## 🧩 Types de Tests Implémentés

### 1. Tests Unitaires (Unit Tests)

**Objectif :** Tester la logique métier de manière isolée (mocks)

**Exemples :**
- `UserServiceImplTest` - Tests du service utilisateur
- `SecurityServiceImplTest` - Tests d'authentification
- `PostServiceImplTest` - Tests de gestion des posts
- `UserValidatorTest` - Tests de validation

**Technologies :**
- JUnit 5 (Jupiter)
- Mockito (mocking)
- AssertJ / Hamcrest (assertions)

**Exécution :**
```bash
mvn test
```

**Exemple de test :**
```java
@Test
@DisplayName("Should save user with encrypted password and USER role")
void testSave_Success() {
    // Given
    when(bCryptPasswordEncoder.encode("password")).thenReturn("encrypted");
    when(roleRepository.findByName("ROLE_USER")).thenReturn(userRole);

    // When
    userService.save(testUser);

    // Then
    verify(userRepository).save(testUser);
    assertEquals("encrypted", testUser.getPassword());
}
```

---

### 2. Tests d'Intégration (Integration Tests)

**Objectif :** Tester l'intégration entre composants avec contexte Spring

**Exemples :**
- `UserRepositoryIntegrationTest` - Tests JPA/Database
- `TimelineControllerIntegrationTest` - Tests MVC complets

**Technologies :**
- `@SpringBootTest` - Contexte Spring complet
- `@DataJpaTest` - Tests JPA avec H2
- `@AutoConfigureMockMvc` - MockMvc avec Spring Security
- H2 Database - Base de données en mémoire

**Exécution :**
```bash
mvn verify
```

**Exemple de test :**
```java
@DataJpaTest
@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByUsername() {
        // Given
        User user = createTestUser("john", "john@example.com");
        entityManager.persist(user);

        // When
        User found = userRepository.findByUsername("john");

        // Then
        assertNotNull(found);
        assertEquals("john", found.getUsername());
    }
}
```

---

### 3. Tests de Sécurité (DevSecOps)

**Objectif :** Vérifier la protection contre les vulnérabilités OWASP Top 10

#### OWASP Top 10 2021 Couvert :

| OWASP | Vulnérabilité | Tests Implémentés |
|-------|---------------|-------------------|
| **A01:2021** | Broken Access Control | ✅ RBAC, Horizontal Privilege Escalation |
| **A02:2021** | Cryptographic Failures | ✅ BCrypt Password Hashing, Salt Verification |
| **A03:2021** | Injection | ✅ SQL Injection, XSS, Command Injection |
| **A04:2021** | Insecure Design | ✅ Input Validation, Length Checks |
| **A07:2021** | Authentication Failures | ✅ Login Security, Session Management |

**Fichiers de tests :**
- `AuthenticationSecurityTest.java` - Authentification et autorisation
- `InjectionSecurityTest.java` - Protection contre injections

**Exécution :**
```bash
# Tous les tests de sécurité
mvn test -Dtest=*SecurityTest

# Seulement les tests d'injection
mvn test -Dtest=InjectionSecurityTest
```

**Exemples de tests :**

#### Protection SQL Injection
```java
@Test
@DisplayName("🛡️ Should prevent SQL injection in username field")
void testSqlInjectionInUsername() {
    // Given - SQL injection attempt
    String sqlInjection = "admin' OR '1'='1";

    // When
    User result = userService.findByUsername(sqlInjection);

    // Then - Should not find user (parameterized query)
    assertNull(result);
}
```

#### Protection XSS
```java
@Test
@DisplayName("🛡️ Should sanitize XSS in username")
void testXssInUsername() {
    String xssPayload = "<script>alert('XSS')</script>";

    mockMvc.perform(post("/registration")
            .param("username", xssPayload)
            .with(csrf()))
        .andExpect(model().hasErrors());
}
```

#### Vérification Cryptographie
```java
@Test
@DisplayName("🔒 Should use BCrypt with salt")
void testPasswordHashing() {
    String password = "myPassword";

    String hash1 = passwordEncoder.encode(password);
    String hash2 = passwordEncoder.encode(password);

    // Hashes should be different (random salt)
    assertNotEquals(hash1, hash2);

    // Both should validate correctly
    assertTrue(passwordEncoder.matches(password, hash1));
    assertTrue(passwordEncoder.matches(password, hash2));
}
```

---

### 4. Tests End-to-End (E2E)

**Objectif :** Tester des parcours utilisateur complets

**Exemple :**
- `UserJourneyE2ETest` - Inscription → Login → Post → Logout

**Scénarios testés :**
- ✅ Inscription complète d'un utilisateur
- ✅ Validation des erreurs de formulaire
- ✅ Création de posts
- ✅ Gestion de session
- ✅ Health checks Kubernetes

**Exécution :**
```bash
mvn test -Dtest=*E2ETest
```

---

## 🚀 Exécution des Tests

### Commandes Maven

```bash
# 1. Compiler le projet
mvn clean compile

# 2. Exécuter SEULEMENT les tests unitaires
mvn test

# 3. Exécuter tous les tests (unitaires + intégration)
mvn verify

# 4. Exécuter un test spécifique
mvn test -Dtest=UserServiceImplTest

# 5. Exécuter tests par pattern
mvn test -Dtest=*SecurityTest

# 6. Générer le rapport de couverture JaCoCo
mvn clean test jacoco:report

# 7. Exécuter tous les tests avec rapport de couverture
mvn clean verify jacoco:report
```

### Rapports générés

```
target/
├── surefire-reports/          # Rapports tests unitaires (XML + TXT)
├── failsafe-reports/          # Rapports tests intégration
├── site/
│   └── jacoco/
│       └── index.html         # Rapport de couverture (ouvrir dans navigateur)
└── jacoco.exec                # Données de couverture binaire
```

### Visualiser la couverture

```bash
# Générer et ouvrir le rapport
mvn jacoco:report
open target/site/jacoco/index.html  # macOS
xdg-open target/site/jacoco/index.html  # Linux
start target/site/jacoco/index.html  # Windows
```

---

## 📊 Couverture de Code

### Configuration JaCoCo (pom.xml)

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.9</version>
    <executions>
        <execution>
            <id>jacoco-initialize</id>
            <phase>process-resources</phase>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>jacoco-site</id>
            <phase>post-integration-test</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Objectifs de Couverture

| Métrique | Seuil Minimum | Objectif Recommandé |
|----------|---------------|---------------------|
| **Line Coverage** | 60% | 80% |
| **Branch Coverage** | 50% | 70% |
| **Complexity Coverage** | 50% | 65% |

### Interpréter les Métriques

```
🟢 Green  (> 80%) - Excellente couverture
🟡 Yellow (60-80%) - Couverture acceptable
🔴 Red    (< 60%) - Couverture insuffisante (pipeline fail)
```

---

## 🔐 Quality Gates CI/CD

### Pipeline Jenkins Amélioré

```groovy
pipeline {
    stages {
        stage('BUILD') {
            steps { sh 'mvn clean install -DskipTests' }
        }

        stage('UNIT TEST') {
            steps { sh 'mvn test' }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
                failure {
                    error('Unit tests must pass!')
                }
            }
        }

        stage('INTEGRATION TEST') {
            steps { sh 'mvn verify -DskipUnitTests' }
            post {
                failure {
                    error('Integration tests must pass!')
                }
            }
        }

        stage('CODE COVERAGE') {
            steps { sh 'mvn jacoco:report' }
            post {
                success {
                    jacoco(
                        minimumLineCoverage: '60',
                        minimumBranchCoverage: '50',
                        changeBuildStatus: true
                    )
                }
            }
        }

        stage('SONARQUBE') {
            steps {
                withSonarQubeEnv('sonar-pro') {
                    sh '''
                        sonar-scanner \
                        -Dsonar.projectKey=facelink \
                        -Dsonar.junit.reportsPath=target/surefire-reports/ \
                        -Dsonar.jacoco.reportsPath=target/jacoco.exec
                    '''
                }
            }
        }

        stage('QUALITY GATE') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
    }
}
```

### Que se passe-t-il si un Quality Gate échoue ?

```
❌ Tests Unitaires FAIL       → Pipeline STOPPED (aucun déploiement)
❌ Tests Intégration FAIL     → Pipeline STOPPED
❌ Couverture < 60%           → Pipeline STOPPED (JaCoCo)
❌ SonarQube Quality Gate     → Pipeline STOPPED
✅ Tous les gates PASS        → Déploiement autorisé ✅
```

---

## 🔒 Tests de Sécurité (DevSecOps)

### Checklist de Sécurité Testée

#### ✅ A01:2021 - Broken Access Control
- [x] RBAC (Role-Based Access Control)
- [x] Protection admin endpoints
- [x] Prévention escalade de privilèges horizontale
- [x] Validation autorisations par rôle

#### ✅ A02:2021 - Cryptographic Failures
- [x] Hashage BCrypt des mots de passe
- [x] Salt aléatoire pour chaque hash
- [x] Pas de stockage plaintext
- [x] Validation force hashage

#### ✅ A03:2021 - Injection
- [x] Protection SQL Injection (JPA paramétrisé)
- [x] Protection XSS (échappement JSTL)
- [x] Protection Command Injection
- [x] Protection LDAP Injection
- [x] Protection NoSQL Injection
- [x] Protection XXE (XML External Entity)

#### ✅ A04:2021 - Insecure Design
- [x] Validation longueur min/max
- [x] Validation patterns malveillants
- [x] Rejet caractères spéciaux dangereux

#### ✅ A07:2021 - Authentication Failures
- [x] Exigence mot de passe fort (≥8 chars)
- [x] Confirmation mot de passe
- [x] Protection CSRF
- [x] Gestion sécurisée des sessions
- [x] Auto-login post-registration
- [x] Invalidation session logout

### Commandes pour Tests de Sécurité

```bash
# Tous les tests de sécurité
mvn test -Dtest=*SecurityTest

# Tests d'authentification uniquement
mvn test -Dtest=AuthenticationSecurityTest

# Tests d'injection uniquement
mvn test -Dtest=InjectionSecurityTest

# Tests avec rapport détaillé
mvn test -Dtest=*SecurityTest -Dsurefire.printSummary=true
```

---

## 🎓 Bonnes Pratiques

### 1. Nommage des Tests

```java
// ✅ BON - Descriptif et clair
@DisplayName("Should save user with encrypted password and ROLE_USER")
void testSave_Success() { }

// ❌ MAUVAIS - Vague
@Test
void test1() { }
```

### 2. Pattern AAA (Arrange-Act-Assert)

```java
@Test
void testCreatePost() {
    // ARRANGE (Given) - Préparer les données
    String content = "Test post";
    User author = createTestUser();
    when(postRepository.save(any())).thenReturn(testPost);

    // ACT (When) - Exécuter l'action
    Post result = postService.createPost(content, null, author);

    // ASSERT (Then) - Vérifier le résultat
    assertNotNull(result);
    assertEquals(content, result.getContent());
    verify(postRepository).save(any(Post.class));
}
```

### 3. Tests Indépendants

```java
// ✅ BON - Chaque test est autonome
@BeforeEach
void setUp() {
    testUser = new User();
    testUser.setUsername("testuser");
}

// ❌ MAUVAIS - Tests dépendants
static User sharedUser; // Éviter l'état partagé modifiable
```

### 4. Utiliser @DisplayName

```java
@DisplayName("🔒 Should prevent SQL injection in login")
void testSqlInjection() { }
```

### 5. Tests de Cas Limites

```java
// Tester les valeurs limites
@Test void testUsernameMinLength() { }  // 5 caractères
@Test void testUsernameMaxLength() { }  // 32 caractères
@Test void testUsernameTooShort() { }   // 4 caractères
@Test void testUsernameTooLong() { }    // 33 caractères
```

---

## 🛠️ Dépannage

### Problème : Tests échouent localement

```bash
# Nettoyer et recompiler
mvn clean install

# Vérifier la base H2 en mémoire
# Les tests utilisent H2, pas MySQL
```

### Problème : JaCoCo ne génère pas de rapport

```bash
# S'assurer que les tests s'exécutent
mvn clean test

# Puis générer le rapport
mvn jacoco:report

# Vérifier que jacoco-maven-plugin est dans pom.xml
```

### Problème : Spring Security Test ne fonctionne pas

```xml
<!-- Vérifier cette dépendance dans pom.xml -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <version>${spring-security.version}</version>
    <scope>test</scope>
</dependency>
```

### Problème : Tests d'intégration échouent

```bash
# Vérifier H2 database
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
    <scope>test</scope>
</dependency>
```

---

## 📚 Ressources Supplémentaires

### Documentation Officielle

- [JUnit 5](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JaCoCo](https://www.jacoco.org/jacoco/trunk/doc/)
- [OWASP Top 10 2021](https://owasp.org/Top10/)

### Outils DevOps

- **SonarQube** - Analyse qualité et sécurité du code
- **Jenkins** - Orchestration CI/CD
- **Nexus** - Gestionnaire d'artefacts
- **JaCoCo** - Couverture de code

---

## 🎯 Exercices pour Apprenants

### Exercice 1 : Créer un Test Unitaire
Créez un test pour `PostLikeService.toggleLike()` qui vérifie :
- Like d'un post non-liké
- Unlike d'un post déjà liké
- Exception si le post n'existe pas

### Exercice 2 : Test de Sécurité
Ajoutez un test qui vérifie la protection contre :
- Path Traversal (`../../etc/passwd`)
- Expression Language Injection (`${7*7}`)

### Exercice 3 : Quality Gate
Configurez un Quality Gate qui échoue si :
- Couverture ligne < 70%
- Couverture branche < 60%
- Bugs critiques > 0

---

## ✅ Checklist DevOps/DevSecOps

Avant de merger votre code :

- [ ] Tous les tests unitaires passent (`mvn test`)
- [ ] Tous les tests d'intégration passent (`mvn verify`)
- [ ] Couverture de code ≥ 60% (JaCoCo)
- [ ] Aucune vulnérabilité de sécurité détectée
- [ ] Checkstyle validé (0 erreurs)
- [ ] SonarQube Quality Gate PASS
- [ ] Pipeline Jenkins réussi
- [ ] Documentation à jour

---

## 🚀 Commandes Rapides

```bash
# Tests complets + rapports
mvn clean verify jacoco:report

# Uniquement tests unitaires
mvn test

# Uniquement tests de sécurité
mvn test -Dtest=*SecurityTest

# Tests avec verbose output
mvn test -X

# Skip tests (SEULEMENT pour dev local, jamais en CI!)
mvn clean install -DskipTests
```

---

## 📞 Support

Pour toute question sur les tests :
1. Consultez d'abord ce guide
2. Vérifiez les logs des tests (`target/surefire-reports/`)
3. Examinez le rapport JaCoCo (`target/site/jacoco/index.html`)
4. Consultez les issues GitHub du projet

---

**Dernière mise à jour :** 2025-01-15
**Auteur :** Claude AI (Formation DevOps/DevSecOps)
**Niveau :** Intermédiaire → Avancé
