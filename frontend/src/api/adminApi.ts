import { apiRequest, type PageResponse } from "./apiClient";
import type { AdminActionLogResponse, EvidenceResponse, ModerationSummaryResponse } from "../types/apiTypes";

export const adminApi = {
  auditLogs: () => apiRequest<PageResponse<AdminActionLogResponse>>("/api/admin/audit-logs"),
  flaggedEvidence: () => apiRequest<EvidenceResponse[]>("/api/admin/moderation/evidence/flagged"),
  archivedEvidence: () => apiRequest<EvidenceResponse[]>("/api/admin/moderation/evidence/archived"),
  moderationSummary: () => apiRequest<ModerationSummaryResponse>("/api/admin/moderation/summary"),
  createModerationNote: (evidenceId: number, request: { reason?: string; details: string }) =>
    apiRequest<AdminActionLogResponse>(`/api/admin/moderation/evidence/${evidenceId}/note`, {
      method: "POST",
      body: request
    })
};
