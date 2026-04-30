# Local Development Guide

## Requirements

- JDK 17 or newer
- Maven 3.9 or newer

If `mvn` is not on PATH in the Codex desktop workspace, use the bundled Maven runtime:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17.0.16+8'
& 'C:\Users\LIPHE\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test
```

## Run Locally

```powershell
mvn spring-boot:run
```

Default base URL:

```text
http://localhost:8080
```

## Useful Environment Overrides

```powershell
$env:ULTRON_JWT_SECRET="replace-with-a-long-local-development-secret"
$env:ULTRON_JWT_EXPIRATION_MINUTES="60"
$env:ULTRON_APP_ENVIRONMENT="local"
$env:ULTRON_STORAGE_MODE="LOCAL"
$env:ULTRON_STORAGE_LOCAL_BASE_PATH="./uploads/ultron-sport"
```

## Test

```powershell
mvn test
```

The default test database is H2 in memory. PostgreSQL remains the production target, but Docker Compose and deployment infrastructure are intentionally deferred from this MVP hardening slice.
