import { apiRequest } from "./apiClient";

export type HealthResponse = {
  status: string;
  application: string;
  environment: string;
  timestamp: string;
};

export type HealthReadinessResponse = {
  status: string;
  database: string;
  timestamp: string;
};

export type HealthVersionResponse = {
  application: string;
  version: string;
  environment: string;
  timestamp: string;
};

export const healthApi = {
  health: () => apiRequest<HealthResponse>("/api/health", { token: null }),
  readiness: () => apiRequest<HealthReadinessResponse>("/api/health/readiness", { token: null }),
  version: () => apiRequest<HealthVersionResponse>("/api/health/version", { token: null })
};
