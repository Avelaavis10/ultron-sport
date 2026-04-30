import { NavLink } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import type { UserRole } from "../types/apiTypes";
import { RoleBadge } from "./RoleBadge";

const roleRoutes: Record<UserRole, string> = {
  ATHLETE: "/athlete",
  COACH: "/coach",
  ORGANISATION: "/organisation",
  SCOUT_AGENT: "/scout",
  ADMIN: "/admin"
};

export function NavBar() {
  const { user, isAuthenticated, logout } = useAuth();

  return (
    <header className="topbar">
      <NavLink className="brand" to="/health">
        Ultron Sport MVP
      </NavLink>
      <nav>
        <NavLink to="/health">Health</NavLink>
        {!isAuthenticated && <NavLink to="/login">Login</NavLink>}
        {!isAuthenticated && <NavLink to="/register">Register</NavLink>}
        {isAuthenticated && <NavLink to="/dashboard">Dashboard</NavLink>}
        {user && <NavLink to={roleRoutes[user.role]}>Role workspace</NavLink>}
        {isAuthenticated && <NavLink to="/notifications">Notifications</NavLink>}
      </nav>
      {user && (
        <div className="session">
          <span>{user.displayName}</span>
          <RoleBadge role={user.role} />
          <button type="button" onClick={logout}>
            Logout
          </button>
        </div>
      )}
    </header>
  );
}
