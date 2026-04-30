import { apiRequest } from "./apiClient";
import type { CoachProfileResponse, CreateCoachProfileRequest, UpdateCoachProfileRequest } from "../types/apiTypes";

export const coachProfileApi = {
  create: (request: CreateCoachProfileRequest) =>
    apiRequest<CoachProfileResponse>("/api/coach-profiles", { method: "POST", body: request }),
  me: () => apiRequest<CoachProfileResponse>("/api/coach-profiles/me"),
  updateMe: (request: UpdateCoachProfileRequest) =>
    apiRequest<CoachProfileResponse>("/api/coach-profiles/me", { method: "PATCH", body: request }),
  getById: (coachProfileId: number) => apiRequest<CoachProfileResponse>(`/api/coach-profiles/${coachProfileId}`)
};
