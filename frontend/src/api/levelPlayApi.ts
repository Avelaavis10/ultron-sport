import { apiRequest } from "./apiClient";
import type { LevelPlayScoreExplanationResponse, LevelPlayScoreResponse } from "../types/apiTypes";

export const levelPlayApi = {
  me: () => apiRequest<LevelPlayScoreResponse>("/api/levelplay/me"),
  getForAthlete: (athleteProfileId: number) =>
    apiRequest<LevelPlayScoreResponse>(`/api/levelplay/athletes/${athleteProfileId}`),
  explain: (athleteProfileId: number) =>
    apiRequest<LevelPlayScoreExplanationResponse>(`/api/levelplay/athletes/${athleteProfileId}/explain`),
  recalculate: (athleteProfileId: number) =>
    apiRequest<LevelPlayScoreResponse>(`/api/levelplay/athletes/${athleteProfileId}/recalculate`, { method: "POST" }),
  recalculateAll: () => apiRequest<LevelPlayScoreResponse[]>("/api/levelplay/recalculate-all", { method: "POST" })
};
