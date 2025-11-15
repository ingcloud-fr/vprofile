# 🧪 Guide Complet des Tests - Facelink (Formation DevOps/DevSecOps/GitOps)

## 📋 Table des matières

1. [Introduction](#introduction)
2. [Installation et Configuration](#installation-et-configuration)
3. [Architecture des Tests](#architecture-des-tests)
4. [Types de Tests Implémentés](#types-de-tests-implémentés)
5. [Exécution des Tests](#exécution-des-tests)
6. [Couverture de Code](#couverture-de-code)
7. [Quality Gates CI/CD](#quality-gates-cicd)
8. [Tests de Sécurité (DevSecOps)](#tests-de-sécurité-devsecops)
9. [Bonnes Pratiques](#bonnes-pratiques)
10. [Dépannage](#dépannage)

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

## 🔧 Installation et Configuration

### Prérequis : Installer Maven et Java

Avant de pouvoir exécuter les tests en local, vous devez installer Maven et Java.

#### Option 1 : Installation via apt (Recommandé pour Ubuntu/Debian)

```bash
# 1. Mettre à jour les paquets
sudo apt update

# 2. Installer Maven et Java
sudo apt install maven default-jdk -y

# 3. Vérifier l'installation
mvn --version
java --version
```

**Résultat attendu :**
```
Apache Maven 3.x.x
Java version: 17 ou 21
```

#### Option 2 : Installation manuelle de Maven

Si la version apt est trop ancienne :

```bash
# 1. Installer Java
sudo apt install default-jdk -y

# 2. Télécharger Maven
cd ~/Downloads
wget https://dlcdn.apache.org/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz

# 3. Extraire et installer
sudo tar xzf apache-maven-3.9.6-bin.tar.gz -C /opt
sudo ln -s /opt/apache-maven-3.9.6 /opt/maven

# 4. Configurer les variables d'environnement
echo 'export M2_HOME=/opt/maven' >> ~/.bashrc
echo 'export PATH=${M2_HOME}/bin:${PATH}' >> ~/.bashrc
source ~/.bashrc

# 5. Vérifier
mvn --version
```

#### Option 3 : Utiliser Docker (SANS installer Maven)

Si vous ne voulez pas installer Maven, utilisez Docker :

```bash
# Depuis le répertoire du projet
cd /chemin/vers/vprofile

# Exécuter les tests avec Docker
docker run --rm \
  -v "$(pwd)":/app \
  -w /app \
  maven:3.9-eclipse-temurin-17 \
  mvn clean test

# Générer le rapport de couverture
docker run --rm \
  -v "$(pwd)":/app \
  -w /app \
  maven:3.9-eclipse-temurin-17 \
  mvn clean verify jacoco:report
```

#### Script d'Exécution avec Docker

Créez un script `run-tests.sh` pour simplifier :

```bash
#!/bin/bash
# Script pour exécuter les tests sans installer Maven

cat > run-tests.sh <<'SCRIPT'
#!/bin/bash
echo "🧪 Exécution des tests avec Docker..."

docker run --rm \
  -v "$(pwd)":/app \
  -w /app \
  maven:3.9-eclipse-temurin-17 \
  mvn clean test jacoco:report

echo ""
echo "✅ Tests terminés!"
echo "📊 Rapport de couverture : target/site/jacoco/index.html"
SCRIPT

chmod +x run-tests.sh
./run-tests.sh
```

### Vérification de l'Installation

```bash
# Vérifier Maven
mvn --version

# Vérifier Java
java --version

# Tester la compilation du projet
cd /chemin/vers/vprofile
mvn clean compile
```

### ⚠️ Points Importants

**Les tests N'ONT PAS BESOIN de services externes :**
- ❌ Pas besoin de MySQL en cours d'exécution
- ❌ Pas besoin de RabbitMQ
- ❌ Pas besoin d'Elasticsearch
- ❌ Pas besoin de Memcached

**Les tests utilisent :**
- ✅ H2 Database (base de données en mémoire)
- ✅ Mocks (Mockito) pour les dépendances
- ✅ Spring Boot Test avec contexte en mémoire

### Résolution de Problèmes d'Installation

#### Problème : "Java version not compatible"

```bash
# Vérifier la version Java
java --version

# Installer Java 17 (requis pour ce projet)
sudo apt install openjdk-17-jdk -y

# Définir Java 17 par défaut
sudo update-alternatives --config java
```

#### Problème : "mvn command not found"

```bash
# Vérifier si Maven est dans le PATH
echo $PATH | grep maven

# Si non, ajouter manuellement
export PATH=/opt/maven/bin:$PATH

# Rendre permanent
echo 'export PATH=/opt/maven/bin:$PATH' >> ~/.bashrc
source ~/.bashrc
```

#### Problème : Tests échouent à cause de dépendances manquantes

```bash
# Forcer le téléchargement des dépendances
mvn dependency:resolve

# Nettoyer et recompiler
mvn clean compile

# Puis relancer les tests
mvn test
```

### Quelle Méthode Choisir ?

| Méthode | Avantages | Inconvénients | Recommandé pour |
|---------|-----------|---------------|-----------------|
| **apt install** | ✅ Simple et rapide<br>✅ Gestion des mises à jour | ⚠️ Version parfois ancienne | Débutants |
| **Installation manuelle** | ✅ Dernière version<br>✅ Contrôle total | ⚠️ Pas de mises à jour auto | Utilisateurs avancés |
| **Docker** | ✅ Aucune installation<br>✅ Isolation complète | ⚠️ Plus lent<br>⚠️ Nécessite Docker | Tests rapides |

**Recommandation pour formation DevOps :** Installez Maven avec apt (Option 1) - c'est un outil DevOps essentiel !

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

### 🎯 Commandes de Test Spécifiques

#### Tests par Catégorie

```bash
# Seulement les tests de sécurité (DevSecOps)
mvn test -Dtest=*SecurityTest

# Seulement les tests unitaires de services
mvn test -Dtest=*ServiceTest

# Seulement les tests d'intégration
mvn test -Dtest=*IntegrationTest

# Seulement les tests end-to-end
mvn test -Dtest=*E2ETest

# Un test spécifique
mvn test -Dtest=UserServiceImplTest

# Exécuter plusieurs tests spécifiques
mvn test -Dtest=UserServiceImplTest,SecurityServiceImplTest
```

#### Voir les Résultats Détaillés

```bash
# Tests avec output verbeux
mvn test -X

# Tests avec résumé détaillé
mvn test -Dsurefire.printSummary=true

# Ne pas stopper au premier échec
mvn test -Dmaven.test.failure.ignore=true

# Afficher les traces d'erreur complètes
mvn test -Dmaven.test.failure.stackTrace=true
```

### 📊 Localiser et Consulter les Rapports

Après exécution, les rapports sont dans :

```
target/
├── surefire-reports/
│   ├── TEST-*.xml                    # Rapports XML (pour CI/CD)
│   └── *.txt                         # Rapports texte lisibles
│
├── site/
│   └── jacoco/
│       ├── index.html                # Rapport de couverture (PAGE PRINCIPALE)
│       ├── jacoco-sessions.html      # Sessions de test
│       └── com.visualpathit.account/ # Détails par package
│
└── jacoco.exec                       # Données binaires JaCoCo
```

#### Consulter les Rapports en Ligne de Commande

```bash
# 1. Rapport de couverture (visuel dans navigateur)
xdg-open target/site/jacoco/index.html

# 2. Rapports de tests (texte)
cat target/surefire-reports/*.txt

# 3. Résumé des tests
grep -A 5 "Tests run:" target/surefire-reports/*.txt

# 4. Voir les tests qui ont échoué
grep -B 2 "FAILURE" target/surefire-reports/*.txt

# 5. Lister tous les tests exécutés
ls -lh target/surefire-reports/TEST-*.xml
```

### 🎓 Guide de Test pour Apprenants

#### Niveau 1 : Tests Unitaires (Débutant)

```bash
# Étape 1 : Commencer par un seul test
mvn test -Dtest=UserServiceImplTest

# Étape 2 : Voir le résultat
cat target/surefire-reports/com.visualpathit.account.serviceTest.UserServiceImplTest.txt

# Étape 3 : Comprendre le résultat
# Tests run: X, Failures: Y, Errors: Z, Skipped: W
```

**Résultat attendu :**
```
-------------------------------------------------------
 T E S T S
-------------------------------------------------------
Running com.visualpathit.account.serviceTest.UserServiceImplTest
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.5 sec

Results :

Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
```

#### Niveau 2 : Tests de Sécurité (Intermédiaire)

```bash
# Étape 1 : Tests DevSecOps (OWASP Top 10)
mvn test -Dtest=*SecurityTest

# Étape 2 : Voir les résultats
ls -lh target/surefire-reports/*SecurityTest.txt

# Étape 3 : Analyser les tests de sécurité
grep "🔒\|🛡️" target/surefire-reports/*SecurityTest.txt
```

**Que tester ?**
- ✅ Protection SQL Injection
- ✅ Protection XSS
- ✅ Validation BCrypt
- ✅ RBAC (Role-Based Access Control)
- ✅ CSRF Protection

#### Niveau 3 : Pipeline Complet (Avancé)

```bash
# Simuler le pipeline CI/CD localement
echo "🚀 Démarrage du pipeline de test..."

# Étape 1 : Build
mvn clean compile && echo "✅ BUILD SUCCESS" || echo "❌ BUILD FAILED"

# Étape 2 : Tests unitaires
mvn test && echo "✅ UNIT TESTS PASSED" || echo "❌ UNIT TESTS FAILED"

# Étape 3 : Tests d'intégration
mvn verify && echo "✅ INTEGRATION TESTS PASSED" || echo "❌ INTEGRATION TESTS FAILED"

# Étape 4 : Rapport de couverture
mvn jacoco:report && echo "✅ COVERAGE REPORT GENERATED" || echo "❌ COVERAGE FAILED"

# Étape 5 : Vérifier la couverture minimum
echo "📊 Vérification des seuils de couverture..."
# (SonarQube ou JaCoCo quality gates)

echo "✅ All quality gates passed!"
```

### 📋 Résumé des Commandes Essentielles

| Action | Commande | Temps estimé |
|--------|----------|--------------|
| **Compiler** | `mvn clean compile` | 30s |
| **Tests unitaires** | `mvn test` | 1-2 min |
| **Tous les tests** | `mvn verify` | 3-5 min |
| **Rapport couverture** | `mvn jacoco:report` | 30s |
| **Tests sécurité** | `mvn test -Dtest=*SecurityTest` | 1 min |
| **Pipeline complet** | `mvn clean verify jacoco:report` | 4-6 min |
| **Avec Docker** | `docker run --rm -v $(pwd):/app -w /app maven:3.9-eclipse-temurin-17 mvn test` | 2-3 min |

### 🔍 Interpréter les Résultats de Tests

#### Test Réussi ✅

```
Tests run: 11, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

#### Test Échoué ❌

```
Tests run: 11, Failures: 1, Errors: 0, Skipped: 0
[ERROR] COMPILATION ERROR
[ERROR] testSave_Success  Time elapsed: 0.1 sec  <<< FAILURE!
java.lang.AssertionError: Expected 5 but was 4
```

**Comment déboguer :**
```bash
# 1. Voir les détails de l'échec
cat target/surefire-reports/com.visualpathit.account.serviceTest.UserServiceImplTest.txt

# 2. Exécuter le test en mode debug
mvn test -Dtest=UserServiceImplTest -X

# 3. Vérifier les logs
tail -f target/surefire-reports/*.txt
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
