import { Link, Navigate } from "react-router-dom";
import { RoleBadge } from "../components/RoleBadge";
import { useAuth } from "../auth/AuthContext";
import type { UserRole } from "../types/apiTypes";

const roleRoute: Record<UserRole, string> = {
  ATHLETE: "/athlete",
  COACH: "/coach",
  ORGANISATION: "/organisation",
  SCOUT_AGENT: "/scout",
  ADMIN: "/admin"
};

export function DashboardPage() {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="page">
      <h1>Dashboard</h1>
      <p>
        Signed in as <strong>{user.displayName}</strong> <RoleBadge role={user.role} />
      </p>
      <div className="actions">
        <Link className="button-link" to={roleRoute[user.role]}>
          Open {user.role} workspace
        </Link>
        <Link className="button-link secondary" to="/notifications">
          Notifications
        </Link>
      </div>
      <section className="panel">
        <h2>MVP Prototype Boundary</h2>
        <p>
          This is a lightweight API validation tool. It intentionally avoids production visual design, push
          notifications, WebSockets, AI, payments, and social features.
        </p>
      </section>
    </div>
  );
}
