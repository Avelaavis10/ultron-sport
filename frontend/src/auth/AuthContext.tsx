import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { authApi } from "../api/authApi";
import { clearStoredSession } from "../api/apiClient";
import { TOKEN_STORAGE_KEY, USER_STORAGE_KEY } from "../config/apiConfig";
import type { AuthResponse, CurrentUserResponse, LoginRequest, RegisterRequest } from "../types/apiTypes";

type AuthContextValue = {
  token: string | null;
  user: CurrentUserResponse | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (request: LoginRequest) => Promise<CurrentUserResponse>;
  register: (request: RegisterRequest) => Promise<CurrentUserResponse>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function readStoredUser(): CurrentUserResponse | null {
  const raw = sessionStorage.getItem(USER_STORAGE_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw) as CurrentUserResponse;
  } catch {
    return null;
  }
}

function authToCurrentUser(auth: AuthResponse): CurrentUserResponse {
  return {
    id: auth.userId,
    displayName: auth.displayName,
    email: auth.email,
    role: auth.role,
    status: "ACTIVE"
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => sessionStorage.getItem(TOKEN_STORAGE_KEY));
  const [user, setUser] = useState<CurrentUserResponse | null>(() => readStoredUser());
  const [isLoading, setIsLoading] = useState(false);

  const persistSession = useCallback((accessToken: string, currentUser: CurrentUserResponse) => {
    sessionStorage.setItem(TOKEN_STORAGE_KEY, accessToken);
    sessionStorage.setItem(USER_STORAGE_KEY, JSON.stringify(currentUser));
    setToken(accessToken);
    setUser(currentUser);
  }, []);

  const logout = useCallback(() => {
    clearStoredSession();
    setToken(null);
    setUser(null);
  }, []);

  useEffect(() => {
    const handler = () => logout();
    window.addEventListener("ultron:unauthorized", handler);
    return () => window.removeEventListener("ultron:unauthorized", handler);
  }, [logout]);

  useEffect(() => {
    if (!token || user) {
      return;
    }
    setIsLoading(true);
    authApi
      .me(token)
      .then((currentUser) => persistSession(token, currentUser))
      .catch(() => logout())
      .finally(() => setIsLoading(false));
  }, [logout, persistSession, token, user]);

  const login = useCallback(
    async (request: LoginRequest) => {
      setIsLoading(true);
      try {
        const response = await authApi.login(request);
        const currentUser = await authApi.me(response.accessToken).catch(() => authToCurrentUser(response));
        persistSession(response.accessToken, currentUser);
        return currentUser;
      } finally {
        setIsLoading(false);
      }
    },
    [persistSession]
  );

  const register = useCallback(
    async (request: RegisterRequest) => {
      setIsLoading(true);
      try {
        const response = await authApi.register(request);
        const currentUser = await authApi.me(response.accessToken).catch(() => authToCurrentUser(response));
        persistSession(response.accessToken, currentUser);
        return currentUser;
      } finally {
        setIsLoading(false);
      }
    },
    [persistSession]
  );

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token && user),
      isLoading,
      login,
      register,
      logout
    }),
    [isLoading, login, logout, register, token, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }
  return context;
}
