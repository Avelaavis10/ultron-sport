import { apiRequest, type PageResponse } from "./apiClient";
import type { CreateOrganisationRequest, OrganisationResponse } from "../types/apiTypes";

export const organisationApi = {
  create: (request: CreateOrganisationRequest) =>
    apiRequest<OrganisationResponse>("/api/organisations", { method: "POST", body: request }),
  search: (query: Record<string, string | number | boolean | null | undefined> = {}) =>
    apiRequest<PageResponse<OrganisationResponse>>("/api/organisations", { query }),
  getById: (organisationId: number) => apiRequest<OrganisationResponse>(`/api/organisations/${organisationId}`),
  update: (organisationId: number, request: Partial<CreateOrganisationRequest> & { verificationStatus?: string }) =>
    apiRequest<OrganisationResponse>(`/api/organisations/${organisationId}`, { method: "PATCH", body: request })
};
