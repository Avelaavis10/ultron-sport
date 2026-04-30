import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it } from "vitest";
import { App } from "../App";
import { AuthProvider } from "../auth/AuthContext";
import { mockBackendFetch } from "../test/mockApi";

function renderRoute(path: string) {
  mockBackendFetch();
  return render(
    <MemoryRouter initialEntries={[path]}>
      <AuthProvider>
        <App />
      </AuthProvider>
    </MemoryRouter>
  );
}

describe("App public smoke routes", () => {
  it("renders the health page", () => {
    renderRoute("/health");

    expect(screen.getByRole("heading", { name: /Ultron Sport MVP Prototype/i })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Refresh health/i })).toBeInTheDocument();
  });

  it("renders the login page", () => {
    renderRoute("/login");

    expect(screen.getByRole("heading", { name: /Login/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/Email/i)).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Login/i })).toBeInTheDocument();
  });

  it("renders the register page", () => {
    renderRoute("/register");

    expect(screen.getByRole("heading", { name: /Register/i })).toBeInTheDocument();
    expect(screen.getByLabelText(/Display name/i)).toBeInTheDocument();
    expect(screen.getByDisplayValue("ATHLETE")).toBeInTheDocument();
  });
});
