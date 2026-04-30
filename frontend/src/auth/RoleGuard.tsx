import { Navigate, Outlet } from "react-router-dom";
import type { UserRole } from "../types/apiTypes";
import { useAuth } from "./AuthContext";

export function RoleGuard({ roles }: { roles: UserRole[] }) {
  const { user } = useAuth();

  if (!user || !roles.includes(user.role)) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Outlet />;
}
