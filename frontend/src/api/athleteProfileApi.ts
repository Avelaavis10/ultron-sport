import { apiRequest, type PageResponse } from "./apiClient";
import type {
  AthleteProfileResponse,
  CreateAthleteProfileRequest,
  LinkAthleteOrganisationRequest,
  UpdateAthleteProfileRequest
} from "../types/apiTypes";

export const athleteProfileApi = {
  create: (request: CreateAthleteProfileRequest) =>
    apiRequest<AthleteProfileResponse>("/api/athlete-profiles", { method: "POST", body: request }),
  me: () => apiRequest<AthleteProfileResponse>("/api/athlete-profiles/me"),
  updateMe: (request: UpdateAthleteProfileRequest) =>
    apiRequest<AthleteProfileResponse>("/api/athlete-profiles/me", { method: "PATCH", body: request }),
  linkOrganisation: (request: LinkAthleteOrganisationRequest) =>
    apiRequest<AthleteProfileResponse>("/api/athlete-profiles/me/organisation", { method: "PATCH", body: request }),
  getById: (athleteProfileId: number) => apiRequest<AthleteProfileResponse>(`/api/athlete-profiles/${athleteProfileId}`),
  list: () => apiRequest<PageResponse<AthleteProfileResponse>>("/api/athlete-profiles")
};
