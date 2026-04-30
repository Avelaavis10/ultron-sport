import { FormEvent, useState } from "react";
import { useNavigate } from "react-router-dom";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
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
      <h1>Register</h1>
      <form className="form" onSubmit={submit}>
        <label>
          Display name
          <input value={displayName} onChange={(event) => setDisplayName(event.target.value)} required />
        </label>
        <label>
          Email
          <input value={email} onChange={(event) => setEmail(event.target.value)} type="email" required />
        </label>
        <label>
          Phone
          <input value={phone} onChange={(event) => setPhone(event.target.value)} />
        </label>
        <label>
          Password
          <input value={password} onChange={(event) => setPassword(event.target.value)} type="password" required />
        </label>
        <label>
          Role
          <select value={role} onChange={(event) => setRole(event.target.value as UserRole)}>
            {roles.map((option) => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </label>
        <button type="submit" disabled={isLoading}>
          {isLoading ? "Registering..." : "Register"}
        </button>
      </form>
      <ApiErrorMessage error={error} />
    </div>
  );
}
