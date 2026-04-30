# Health Checks

## Purpose

The MVP exposes small public health endpoints for local development, smoke testing, and simple handover checks. These are custom API endpoints, not enterprise observability.

## Endpoints

```http
GET /api/health
GET /api/health/readiness
GET /api/health/version
```

## Examples

```http
GET http://localhost:8080/api/health
```

```json
{
  "status": "UP",
  "application": "Ultron Sport API",
  "environment": "local",
  "timestamp": "2026-04-30T00:00:00Z"
}
```

```http
GET http://localhost:8080/api/health/readiness
```

```json
{
  "status": "READY",
  "database": "UP",
  "timestamp": "2026-04-30T00:00:00Z"
}
```

```http
GET http://localhost:8080/api/health/version
```

```json
{
  "application": "Ultron Sport API",
  "version": "0.1.0-mvp",
  "environment": "local",
  "timestamp": "2026-04-30T00:00:00Z"
}
```

## Configuration

```yaml
ultron:
  app:
    name: Ultron Sport API
    version: 0.1.0-mvp
    environment: local
```

The values can be overridden with `ULTRON_APP_NAME`, `ULTRON_APP_VERSION`, and `ULTRON_APP_ENVIRONMENT`.

## Boundary

This slice does not add Prometheus, Grafana, ELK, OpenTelemetry, distributed tracing, SIEM, alerting, Kubernetes probes, or external monitoring platforms.
