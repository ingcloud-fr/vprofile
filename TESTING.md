# Testing Strategy for DevOps Training

## Overview
This application is configured for DevOps/DevSecOps/GitOps training. The test suite has been simplified to focus on CI/CD pipeline validation rather than exhaustive testing.

## Test Configuration

### Enabled Tests (75 tests)
Simple unit tests that validate core functionality:
- `UserTest.java` - User model validation
- `RoleTest.java` - Role model validation
- `SampleTest.java` - Basic controller tests
- `UserValidatorTest.java` - Validation logic
- `SecurityServiceImplTest.java` - Security service unit tests
- `PostServiceImplTest.java` - Post service unit tests
- `PostLikeServiceTest.java` - Like service unit tests
- `UserControllerTest.java` - Controller unit tests
- `UserServiceImplTest.java` - User service unit tests

### Disabled Tests (68 tests - in `_disabled` folder)
Complex integration tests requiring full ApplicationContext:
- `TimelineControllerIntegrationTest.java` - Timeline integration tests
- `UserJourneyE2ETest.java` - End-to-end user journey tests
- `UserRepositoryIntegrationTest.java` - Repository integration tests
- `AuthenticationSecurityTest.java` - Security integration tests
- `InjectionSecurityTest.java` - Security vulnerability tests

## Running Tests

```bash
# Run all enabled tests
mvn test

# Run tests with coverage report
mvn clean test jacoco:report

# Build the application
mvn clean package
```

## Why This Approach?

For DevOps training purposes:
1. Fast feedback loop for CI/CD pipelines
2. No need for complex test infrastructure setup
3. Focuses on pipeline mechanics rather than comprehensive testing
4. Tests complete in seconds instead of minutes

## Re-enabling Integration Tests

If you need to run the full test suite, modify `pom.xml` and remove the exclusion:

```xml
<excludes>
    <exclude>**/_disabled/**/*.java</exclude>
</excludes>
```
