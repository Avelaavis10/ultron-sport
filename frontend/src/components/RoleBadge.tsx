import type { UserRole } from "../types/apiTypes";

export function RoleBadge({ role }: { role: UserRole }) {
  return <span className="role-badge" aria-label={`Current role ${role}`}>{role}</span>;
}
