import { apiRequest, type PageResponse } from "./apiClient";
import type { AchievementResponse, CreateAchievementRequest } from "../types/apiTypes";

export const achievementApi = {
  create: (request: CreateAchievementRequest) =>
    apiRequest<AchievementResponse>("/api/achievements", { method: "POST", body: request }),
  my: () => apiRequest<AchievementResponse[]>("/api/achievements/my"),
  update: (achievementId: number, request: Omit<CreateAchievementRequest, "athleteProfileId">) =>
    apiRequest<AchievementResponse>(`/api/achievements/${achievementId}`, { method: "PATCH", body: request }),
  listForAthlete: (athleteProfileId: number) =>
    apiRequest<AchievementResponse[]>(`/api/athlete-profiles/${athleteProfileId}/achievements`),
  listAll: () => apiRequest<PageResponse<AchievementResponse>>("/api/achievements")
};
