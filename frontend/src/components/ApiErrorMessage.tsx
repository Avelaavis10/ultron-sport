import { ApiClientError } from "../api/apiClient";

export function ApiErrorMessage({ error }: { error: unknown }) {
  if (!error) {
    return null;
  }

  if (error instanceof ApiClientError) {
    const entries = Object.entries(error.validationErrors);
    return (
      <div className="alert error" role="alert">
        <strong>{error.status === 403 ? "Forbidden" : "Request failed"}</strong>
        <p>{error.message}</p>
        <small>
          Status {error.status}
          {error.apiError?.code ? ` - ${error.apiError.code}` : ""}
        </small>
        {error.apiError?.traceId && <small>Trace ID: {error.apiError.traceId}</small>}
        {entries.length > 0 && (
          <ul>
            {entries.map(([field, message]) => (
              <li key={field}>
                {field}: {message}
              </li>
            ))}
          </ul>
        )}
      </div>
    );
  }

  return (
    <div className="alert error" role="alert">
      {error instanceof Error ? error.message : "Unexpected error"}
    </div>
  );
}
