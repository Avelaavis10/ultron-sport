import type { UserRole } from "../types/apiTypes";

export function RoleBadge({ role }: { role: UserRole }) {
  return <span className="role-badge">{role}</span>;
}
