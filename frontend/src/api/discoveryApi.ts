import { apiRequest, type PageResponse } from "./apiClient";
import type {
  AthleteDiscoveryCardResponse,
  AthleteDiscoveryProfileResponse,
  EvidenceDiscoveryCardResponse
} from "../types/apiTypes";

type DiscoveryQuery = Record<string, string | number | boolean | null | undefined>;

export const discoveryApi = {
  searchAthletes: (query: DiscoveryQuery = {}) =>
    apiRequest<PageResponse<AthleteDiscoveryCardResponse>>("/api/discovery/athletes", { query }),
  getAthleteProfile: (athleteProfileId: number) =>
    apiRequest<AthleteDiscoveryProfileResponse>(`/api/discovery/athletes/${athleteProfileId}`),
  searchEvidence: (query: DiscoveryQuery = {}) =>
    apiRequest<PageResponse<EvidenceDiscoveryCardResponse>>("/api/discovery/evidence", { query })
};
