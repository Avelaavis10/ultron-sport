import { Link, Navigate } from "react-router-dom";
import { PageHeader } from "../components/PageHeader";
import { RoleBadge } from "../components/RoleBadge";
import { WorkflowHint } from "../components/WorkflowHint";
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
      <PageHeader title="Dashboard" description="Choose the role workspace or check notifications while testing the MVP.">
        <RoleBadge role={user.role} />
      </PageHeader>
      <p>
        Signed in as <strong>{user.displayName}</strong>.
      </p>
      <div className="actions">
        <Link className="button-link" to={roleRoute[user.role]}>
          Open {user.role} workspace
        </Link>
        <Link className="button-link secondary" to="/notifications">
          Notifications
        </Link>
      </div>
      <WorkflowHint
        title="Manual testing rhythm"
        steps={[
          "Open the role workspace for the current test user.",
          "Complete the numbered sections from top to bottom.",
          "Use notifications and LevelPlay panels to confirm backend side effects."
        ]}
      />
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
