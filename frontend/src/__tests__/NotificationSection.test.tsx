import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { NotificationSection } from "../components/sections/NotificationSection";

function mockNotificationFetch() {
  return vi.spyOn(globalThis, "fetch").mockImplementation((input: RequestInfo | URL, init?: RequestInit) => {
    const url = new URL(String(input), "http://localhost:8080");

    if (url.pathname === "/api/notifications" && (!init || init.method === undefined || init.method === "GET")) {
      return Promise.resolve(
        new Response(
          JSON.stringify({
            content: [
              {
                id: 1,
                recipientUserId: 11,
                type: "EVIDENCE_VERIFIED",
                title: "Evidence verified",
                message: "Your evidence was verified.",
                status: "UNREAD",
                targetType: "EVIDENCE",
                targetId: 44,
                createdAt: "2026-04-30T00:00:00Z"
              }
            ],
            page: 0,
            size: 20,
            totalElements: 1,
            totalPages: 1,
            sortBy: "createdAt",
            sortDirection: "DESC"
          }),
          { status: 200, headers: { "Content-Type": "application/json" } }
        )
      );
    }

    if (url.pathname === "/api/notifications/unread-count") {
      return Promise.resolve(new Response(JSON.stringify({ unreadCount: 1 }), { status: 200, headers: { "Content-Type": "application/json" } }));
    }

    if (url.pathname === "/api/notifications/1/read" || url.pathname === "/api/notifications/read-all") {
      return Promise.resolve(new Response(JSON.stringify({}), { status: 200, headers: { "Content-Type": "application/json" } }));
    }

    return Promise.resolve(new Response(JSON.stringify({}), { status: 200, headers: { "Content-Type": "application/json" } }));
  });
}

describe("NotificationSection", () => {
  it("renders notifications from the mocked API and supports mark read", async () => {
    const fetchMock = mockNotificationFetch();
    const user = userEvent.setup();

    render(<NotificationSection title="Test Notifications" />);

    expect(await screen.findByRole("heading", { name: /Test Notifications/i })).toBeInTheDocument();
    expect(await screen.findByText(/Evidence verified/i)).toBeInTheDocument();
    expect(screen.getByText(/Unread: 1/i)).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: /Mark read/i }));

    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalledWith(expect.stringContaining("/api/notifications/1/read"), expect.objectContaining({ method: "POST" }));
    });
  });
});
