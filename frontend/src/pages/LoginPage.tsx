import { FormEvent, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { FormField } from "../components/FormField";
import { PageHeader } from "../components/PageHeader";
import { useAuth } from "../auth/AuthContext";
import type { UserRole } from "../types/apiTypes";

const roleRoute: Record<UserRole, string> = {
  ATHLETE: "/athlete",
  COACH: "/coach",
  ORGANISATION: "/organisation",
  SCOUT_AGENT: "/scout",
  ADMIN: "/admin"
};

export function LoginPage() {
  const { login, isLoading } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("athlete@ultronsport.test");
  const [password, setPassword] = useState("password123");
  const [error, setError] = useState<unknown>(null);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      const user = await login({ email, password });
      const state = location.state as { from?: { pathname?: string } } | null;
      navigate(state?.from?.pathname ?? roleRoute[user.role] ?? "/dashboard", { replace: true });
    } catch (err) {
      setError(err);
    }
  }

  return (
    <div className="page narrow">
      <PageHeader
        title="Login"
        description="Use a test account to validate role-specific MVP workflows. The access token is stored in sessionStorage for this prototype only."
      />
      <form className="form" onSubmit={submit}>
        <FormField label="Email" required hint="Use one of the seeded/manual test users, for example athlete@ultronsport.test.">
          <input value={email} onChange={(event) => setEmail(event.target.value)} type="email" required />
        </FormField>
        <FormField label="Password" required hint="Default manual testing password is usually password123.">
          <input value={password} onChange={(event) => setPassword(event.target.value)} type="password" required />
        </FormField>
        <button type="submit" disabled={isLoading}>
          {isLoading ? "Logging in..." : "Login"}
        </button>
      </form>
      <ApiErrorMessage error={error} />
    </div>
  );
}
