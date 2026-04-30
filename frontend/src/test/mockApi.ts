import { vi } from "vitest";
import type { UserRole } from "../types/apiTypes";

const API_BASE_URL = "http://localhost:8080";

export function seedAuthenticatedUser(role: UserRole) {
  sessionStorage.setItem("ultron.accessToken", `test-token-${role}`);
  sessionStorage.setItem(
    "ultron.currentUser",
    JSON.stringify({
      id: role === "ATHLETE" ? 11 : role === "COACH" ? 12 : role === "ORGANISATION" ? 13 : role === "SCOUT_AGENT" ? 14 : 15,
      displayName: `${role} User`,
      email: `${role.toLowerCase()}@ultronsport.test`,
      role,
      status: "ACTIVE"
    })
  );
}

function jsonResponse(body: unknown, status = 200) {
  return Promise.resolve(
    new Response(JSON.stringify(body), {
      status,
      headers: { "Content-Type": "application/json" }
    })
  );
}

function page<T>(content: T[] = []) {
  return {
    content,
    page: 0,
    size: 20,
    totalElements: content.length,
    totalPages: content.length ? 1 : 0,
    sortBy: "createdAt",
    sortDirection: "DESC"
  };
}

export function mockBackendFetch() {
  return vi.spyOn(globalThis, "fetch").mockImplementation((input: RequestInfo | URL) => {
    const url = new URL(String(input), API_BASE_URL);
    const path = url.pathname;

    if (path === "/api/health") {
      return jsonResponse({ status: "UP", application: "Ultron Sport API", environment: "test", timestamp: "2026-04-30T00:00:00Z" });
    }
    if (path === "/api/health/readiness") {
      return jsonResponse({ status: "READY", database: "UP", timestamp: "2026-04-30T00:00:00Z" });
    }
    if (path === "/api/health/version") {
      return jsonResponse({ application: "Ultron Sport API", version: "0.1.0-mvp", timestamp: "2026-04-30T00:00:00Z" });
    }
    if (path === "/api/auth/me") {
      return jsonResponse({ id: 99, displayName: "Test User", email: "test@ultronsport.test", role: "ATHLETE", status: "ACTIVE" });
    }
    if (path === "/api/notifications") {
      return jsonResponse(page([]));
    }
    if (path === "/api/notifications/unread-count") {
      return jsonResponse({ unreadCount: 0 });
    }
    if (path === "/api/athlete-profiles/me") {
      return jsonResponse({
        id: 1,
        userId: 11,
        sport: "Football",
        position: "Forward",
        age: 19,
        gender: "Female",
        location: "Cape Town",
        schoolOrClub: "Ultron Football Academy",
        organisationId: null,
        bio: "Prototype athlete",
        profileCompletenessScore: 75,
        verificationStatus: "VERIFIED"
      });
    }
    if (path === "/api/achievements/my") {
      return jsonResponse([]);
    }
    if (path === "/api/evidence/my" || path === "/api/evidence/pending-verification") {
      return jsonResponse([]);
    }
    if (path === "/api/levelplay/me" || /^\/api\/levelplay\/athletes\/\d+$/.test(path)) {
      return jsonResponse({
        id: 1,
        athleteProfileId: 1,
        verifiedEvidenceCount: 1,
        coachVerificationCount: 1,
        achievementCount: 1,
        profileCompletenessScore: 75,
        evidenceScore: 20,
        achievementScore: 10,
        verificationScore: 10,
        profileCompletenessContribution: 15,
        engagementScore: 0,
        finalCredibilityScore: 55,
        tier: "GOLD",
        calculatedAt: "2026-04-30T00:00:00Z"
      });
    }
    if (/^\/api\/levelplay\/athletes\/\d+\/explain$/.test(path)) {
      return jsonResponse({
        athleteProfileId: 1,
        verifiedEvidenceCount: 1,
        verifiedEvidenceCountScore: 20,
        achievementCount: 1,
        achievementScore: 10,
        coachVerificationCount: 1,
        coachVerificationScore: 10,
        profileCompletenessScore: 75,
        profileCompletenessContribution: 15,
        finalCredibilityScore: 55,
        tier: "GOLD",
        explanationText: "Smoke test explanation.",
        calculatedAt: "2026-04-30T00:00:00Z"
      });
    }
    if (path === "/api/coach-profiles/me") {
      return jsonResponse({
        id: 2,
        userId: 12,
        certificationReference: "MVP-COACH-001",
        organisationId: 1,
        organisationName: "Ultron Football Academy",
        sport: "Football",
        qualificationSummary: "Smoke test coach",
        yearsExperience: 4,
        verificationStatus: "VERIFIED"
      });
    }
    if (path === "/api/organisations") {
      return jsonResponse(page([]));
    }
    if (path === "/api/discovery/athletes" || path === "/api/discovery/evidence") {
      return jsonResponse(page([]));
    }
    if (path === "/api/admin/moderation/summary") {
      return jsonResponse({
        flaggedEvidenceCount: 0,
        archivedEvidenceCount: 0,
        pendingVerificationCount: 0,
        verifiedEvidenceCount: 0,
        rejectedEvidenceCount: 0
      });
    }
    if (path === "/api/admin/moderation/evidence/flagged" || path === "/api/admin/moderation/evidence/archived") {
      return jsonResponse([]);
    }
    if (path === "/api/admin/audit-logs") {
      return jsonResponse(page([]));
    }

    return jsonResponse({}, 200);
  });
}
