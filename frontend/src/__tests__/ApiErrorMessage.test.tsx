import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ApiClientError } from "../api/apiClient";
import { ApiErrorMessage } from "../components/ApiErrorMessage";

describe("ApiErrorMessage", () => {
  it("renders standard API error details and validation messages", () => {
    const error = new ApiClientError("Validation failed", 400, {
      timestamp: "2026-04-30T00:00:00Z",
      status: 400,
      error: "Bad Request",
      message: "Validation failed",
      path: "/api/evidence",
      code: "VALIDATION_ERROR",
      traceId: "trace-123",
      validationErrors: {
        title: "must not be blank"
      }
    });

    render(<ApiErrorMessage error={error} />);

    expect(screen.getByRole("alert")).toBeInTheDocument();
    expect(screen.getByText(/Validation failed/i)).toBeInTheDocument();
    expect(screen.getByText(/Status 400 - VALIDATION_ERROR/i)).toBeInTheDocument();
    expect(screen.getByText(/title: must not be blank/i)).toBeInTheDocument();
  });

  it("renders forbidden errors with a clear label", () => {
    render(<ApiErrorMessage error={new ApiClientError("Forbidden", 403)} />);

    expect(screen.getAllByText(/Forbidden/i).length).toBeGreaterThan(0);
  });
});
