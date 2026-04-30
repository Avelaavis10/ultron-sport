import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { FormField } from "../components/FormField";
import { PageHeader } from "../components/PageHeader";
import { useAuth } from "../auth/AuthContext";
import type { UserRole } from "../types/apiTypes";

const roles: UserRole[] = ["ATHLETE", "COACH", "ORGANISATION", "SCOUT_AGENT", "ADMIN"];
const roleRoute: Record<UserRole, string> = {
  ATHLETE: "/athlete",
  COACH: "/coach",
  ORGANISATION: "/organisation",
  SCOUT_AGENT: "/scout",
  ADMIN: "/admin"
};

export function RegisterPage() {
  const { register, isLoading } = useAuth();
  const navigate = useNavigate();
  const [displayName, setDisplayName] = useState("Athlete User");
  const [email, setEmail] = useState("athlete@ultronsport.test");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("password123");
  const [role, setRole] = useState<UserRole>("ATHLETE");
  const [error, setError] = useState<unknown>(null);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      const user = await register({ displayName, email, phone: phone || null, password, role });
      navigate(roleRoute[user.role] ?? "/dashboard", { replace: true });
    } catch (err) {
      setError(err);
    }
  }

  return (
    <div className="page narrow">
      <PageHeader title="Register" description="Create a local MVP test user, then the prototype will route to that role workspace." />
      <form className="form" onSubmit={submit}>
        <FormField label="Display name" required>
          <input value={displayName} onChange={(event) => setDisplayName(event.target.value)} required />
        </FormField>
        <FormField label="Email" required hint="Use a unique email for each test role.">
          <input value={email} onChange={(event) => setEmail(event.target.value)} type="email" required />
        </FormField>
        <FormField label="Phone" hint="Optional for MVP testing.">
          <input value={phone} onChange={(event) => setPhone(event.target.value)} />
        </FormField>
        <FormField label="Password" required>
          <input value={password} onChange={(event) => setPassword(event.target.value)} type="password" required />
        </FormField>
        <FormField label="Role" required hint="Choose the role whose dashboard you want to test next.">
          <select value={role} onChange={(event) => setRole(event.target.value as UserRole)}>
            {roles.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </FormField>
        <button type="submit" disabled={isLoading}>
          {isLoading ? "Registering..." : "Register"}
        </button>
      </form>
      <ApiErrorMessage error={error} />
    </div>
  );
}
