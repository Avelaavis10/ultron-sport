import { apiRequest } from "./apiClient";
import type { AuthResponse, CurrentUserResponse, LoginRequest, RegisterRequest } from "../types/apiTypes";

export const authApi = {
  register: (request: RegisterRequest) =>
    apiRequest<AuthResponse>("/api/auth/register", { method: "POST", body: request, token: null }),
  login: (request: LoginRequest) =>
    apiRequest<AuthResponse>("/api/auth/login", { method: "POST", body: request, token: null }),
  me: (token?: string | null) => apiRequest<CurrentUserResponse>("/api/auth/me", { token })
};
