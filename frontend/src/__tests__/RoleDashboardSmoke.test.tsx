import { render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it } from "vitest";
import { App } from "../App";
import { AuthProvider } from "../auth/AuthContext";
import type { UserRole } from "../types/apiTypes";
import { mockBackendFetch, seedAuthenticatedUser } from "../test/mockApi";

const roleCases: Array<{ role: UserRole; path: string; heading: RegExp; navLabel: RegExp }> = [
  { role: "ATHLETE", path: "/athlete", heading: /Athlete Workspace/i, navLabel: /Athlete workspace/i },
  { role: "COACH", path: "/coach", heading: /Coach Workspace/i, navLabel: /Coach workspace/i },
  { role: "SCOUT_AGENT", path: "/scout", heading: /Scout Workspace/i, navLabel: /Scout workspace/i },
  { role: "ORGANISATION", path: "/organisation", heading: /Organisation Workspace/i, navLabel: /Organisation workspace/i },
  { role: "ADMIN", path: "/admin", heading: /Admin Workspace/i, navLabel: /Admin workspace/i }
];

function renderAuthenticatedRoute(role: UserRole, path: string) {
  seedAuthenticatedUser(role);
  mockBackendFetch();
  return render(
    <MemoryRouter initialEntries={[path]}>
      <AuthProvider>
        <App />
      </AuthProvider>
    </MemoryRouter>
  );
}

describe("role dashboard smoke routes", () => {
  it("renders the authenticated dashboard shell", () => {
    renderAuthenticatedRoute("ATHLETE", "/dashboard");

    expect(screen.getByRole("heading", { name: /Dashboard/i })).toBeInTheDocument();
    expect(screen.getAllByText(/ATHLETE User/i).length).toBeGreaterThan(0);
    expect(screen.getAllByText("ATHLETE").length).toBeGreaterThan(0);
  });

  it.each(roleCases)("renders $role workspace", async ({ role, path, heading, navLabel }) => {
    renderAuthenticatedRoute(role, path);

    expect(await screen.findByRole("heading", { name: heading })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: navLabel })).toBeInTheDocument();
    expect(screen.getAllByText(role).length).toBeGreaterThan(0);
  });

  it("redirects a wrong-role workspace request back to dashboard", async () => {
    renderAuthenticatedRoute("ATHLETE", "/admin");

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: /Dashboard/i })).toBeInTheDocument();
    });
    expect(screen.getByRole("link", { name: /Open ATHLETE workspace/i })).toBeInTheDocument();
  });

  it("redirects unauthenticated protected routes to login", async () => {
    mockBackendFetch();
    render(
      <MemoryRouter initialEntries={["/dashboard"]}>
        <AuthProvider>
          <App />
        </AuthProvider>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: /Login/i })).toBeInTheDocument();
    });
  });
});
