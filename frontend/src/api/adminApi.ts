import { apiRequest, type PageResponse } from "./apiClient";
import type {
  AdminActionLogResponse,
  AdminAuditLogQuery,
  EvidenceResponse,
  ModerationSummaryResponse
} from "../types/apiTypes";

export const adminApi = {
  auditLogs: (query: AdminAuditLogQuery = {}) =>
    apiRequest<PageResponse<AdminActionLogResponse>>("/api/admin/audit-logs", { query }),
  auditLog: (auditLogId: number) => apiRequest<AdminActionLogResponse>(`/api/admin/audit-logs/${auditLogId}`),
  auditLogsForTarget: (targetType: string, targetId: number) =>
    apiRequest<AdminActionLogResponse[]>(`/api/admin/audit-logs/target/${targetType}/${targetId}`),
  flaggedEvidence: () => apiRequest<EvidenceResponse[]>("/api/admin/moderation/evidence/flagged"),
  archivedEvidence: () => apiRequest<EvidenceResponse[]>("/api/admin/moderation/evidence/archived"),
  moderationSummary: () => apiRequest<ModerationSummaryResponse>("/api/admin/moderation/summary"),
  createModerationNote: (evidenceId: number, request: { reason?: string; details: string }) =>
    apiRequest<AdminActionLogResponse>(`/api/admin/moderation/evidence/${evidenceId}/note`, {
      method: "POST",
      body: request
    })
};
