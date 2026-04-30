# Error Handling

## Standard Response

All API errors use one JSON shape:

```json
{
  "timestamp": "2026-04-30T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed.",
  "path": "/api/auth/register",
  "code": "VALIDATION_FAILED",
  "traceId": "6f877640-c486-421d-a01c-0964681e5a2f",
  "validationErrors": {
    "email": "must be a well-formed email address"
  }
}
```

`validationErrors` is an object keyed by field or validation target. It is empty when the error is not field-specific.

## Common Codes

- `VALIDATION_FAILED`
- `MALFORMED_REQUEST`
- `INVALID_PARAMETER`
- `MISSING_PARAMETER`
- `INVALID_STATE`
- `RESOURCE_NOT_FOUND`
- `DUPLICATE_RESOURCE`
- `AUTHENTICATION_REQUIRED`
- `AUTHENTICATION_FAILED`
- `BAD_CREDENTIALS`
- `ACCESS_DENIED`
- `UNSUPPORTED_MEDIA_TYPE`
- `METHOD_NOT_ALLOWED`
- `UNEXPECTED_ERROR`

## Security

Error responses must not expose stack traces, JWT internals, password details, local filesystem paths, or raw internal metadata. The `traceId` is generated per error response to help developers correlate reports during MVP testing.
