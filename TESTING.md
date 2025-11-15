# Testing Strategy for DevOps Training

## Overview
This application is configured for DevOps/DevSecOps/GitOps training. The test suite has been simplified to focus on CI/CD pipeline validation rather than exhaustive testing.

## Test Configuration

### Active Tests (75 unit tests)
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

### Removed Tests
Complex integration tests have been removed for training simplicity:
- Timeline integration tests (12 tests)
- End-to-end user journey tests (10 tests)
- Repository integration tests (14 tests)
- Authentication security tests (16 tests)
- Injection security tests (16 tests)

These tests required full ApplicationContext, database connections, and complex infrastructure setup. They are available in Git history if needed for production use.

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

## Restoring Integration Tests

If you need the full test suite for production use, you can restore the deleted tests from Git history:

```bash
# Find the commit before deletion
git log --oneline -- src/test/java/com/visualpathit/account/_disabled

# Restore the tests from a specific commit
git checkout <commit-hash> -- src/test/java/com/visualpathit/account/_disabled
```
