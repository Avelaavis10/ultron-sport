import { apiRequest } from "./apiClient";
import type {
  CreateEvidenceRequest,
  EvidenceResponse,
  FlagEvidenceRequest,
  RejectEvidenceRequest,
  VerificationActionResponse,
  VerificationContextResponse
} from "../types/apiTypes";

export const evidenceApi = {
  create: (request: CreateEvidenceRequest) =>
    apiRequest<EvidenceResponse>("/api/evidence", { method: "POST", body: request }),
  getById: (evidenceId: number) => apiRequest<EvidenceResponse>(`/api/evidence/${evidenceId}`),
  my: () => apiRequest<EvidenceResponse[]>("/api/evidence/my"),
  submit: (evidenceId: number) =>
    apiRequest<VerificationActionResponse>(`/api/evidence/${evidenceId}/submit`, { method: "POST" }),
  attachMedia: (evidenceId: number, mediaId: number) =>
    apiRequest<EvidenceResponse>(`/api/evidence/${evidenceId}/media/${mediaId}`, { method: "POST" }),
  pendingVerification: () => apiRequest<EvidenceResponse[]>("/api/evidence/pending-verification"),
  verify: (evidenceId: number) =>
    apiRequest<VerificationActionResponse>(`/api/evidence/${evidenceId}/verify`, { method: "POST" }),
  reject: (evidenceId: number, request: RejectEvidenceRequest) =>
    apiRequest<VerificationActionResponse>(`/api/evidence/${evidenceId}/reject`, { method: "POST", body: request }),
  flag: (evidenceId: number, request: FlagEvidenceRequest) =>
    apiRequest<VerificationActionResponse>(`/api/evidence/${evidenceId}/flag`, { method: "POST", body: request }),
  archive: (evidenceId: number) =>
    apiRequest<VerificationActionResponse>(`/api/evidence/${evidenceId}/archive`, { method: "POST" }),
  verificationContext: (evidenceId: number) =>
    apiRequest<VerificationContextResponse>(`/api/evidence/${evidenceId}/verification-context`)
};
