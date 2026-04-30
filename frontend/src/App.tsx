import { Navigate, Route, Routes } from "react-router-dom";
import { RequireAuth } from "./auth/RequireAuth";
import { RoleGuard } from "./auth/RoleGuard";
import { Layout } from "./components/Layout";
import { AdminDashboard } from "./pages/AdminDashboard";
import { AthleteDashboard } from "./pages/AthleteDashboard";
import { CoachDashboard } from "./pages/CoachDashboard";
import { DashboardPage } from "./pages/DashboardPage";
import { HealthPage } from "./pages/HealthPage";
import { LoginPage } from "./pages/LoginPage";
import { NotFoundPage } from "./pages/NotFoundPage";
import { NotificationsPage } from "./pages/NotificationsPage";
import { OrganisationDashboard } from "./pages/OrganisationDashboard";
import { RegisterPage } from "./pages/RegisterPage";
import { ScoutDashboard } from "./pages/ScoutDashboard";

export function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route index element={<Navigate to="/health" replace />} />
        <Route path="/health" element={<HealthPage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        <Route element={<RequireAuth />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/notifications" element={<NotificationsPage />} />

          <Route element={<RoleGuard roles={["ATHLETE"]} />}>
            <Route path="/athlete" element={<AthleteDashboard />} />
          </Route>
          <Route element={<RoleGuard roles={["COACH"]} />}>
            <Route path="/coach" element={<CoachDashboard />} />
          </Route>
          <Route element={<RoleGuard roles={["SCOUT_AGENT"]} />}>
            <Route path="/scout" element={<ScoutDashboard />} />
          </Route>
          <Route element={<RoleGuard roles={["ORGANISATION"]} />}>
            <Route path="/organisation" element={<OrganisationDashboard />} />
          </Route>
          <Route element={<RoleGuard roles={["ADMIN"]} />}>
            <Route path="/admin" element={<AdminDashboard />} />
          </Route>
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
