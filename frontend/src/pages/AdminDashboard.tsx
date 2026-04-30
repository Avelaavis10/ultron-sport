import { FormEvent, useEffect, useState } from "react";
import { adminApi } from "../api/adminApi";
import { evidenceApi } from "../api/evidenceApi";
import { levelPlayApi } from "../api/levelPlayApi";
import { notificationApi } from "../api/notificationApi";
import { organisationApi } from "../api/organisationApi";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { DataBlock } from "../components/DataBlock";
import { LoadingState } from "../components/LoadingState";
import type {
  AdminActionLogResponse,
  CreateOrganisationRequest,
  EvidenceResponse,
  ModerationSummaryResponse,
  PageResponse
} from "../types/apiTypes";

const defaultOrganisation: CreateOrganisationRequest = {
  name: "Ultron Football Academy",
  type: "ACADEMY",
  location: "Cape Town",
  contactEmail: "admin@ultronsport.test",
  primaryAdminUserId: null
};

export function AdminDashboard() {
  const [organisationForm, setOrganisationForm] = useState<CreateOrganisationRequest>(defaultOrganisation);
  const [summary, setSummary] = useState<ModerationSummaryResponse | null>(null);
  const [flagged, setFlagged] = useState<EvidenceResponse[]>([]);
  const [archived, setArchived] = useState<EvidenceResponse[]>([]);
  const [auditLogs, setAuditLogs] = useState<PageResponse<AdminActionLogResponse> | null>(null);
  const [evidenceId, setEvidenceId] = useState("");
  const [flagReason, setFlagReason] = useState("Flagged during MVP prototype validation.");
  const [noteDetails, setNoteDetails] = useState("Internal moderation note from React prototype.");
  const [recalculateAthleteProfileId, setRecalculateAthleteProfileId] = useState("");
  const [unreadCount, setUnreadCount] = useState<number | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [summaryResponse, flaggedResponse, archivedResponse, logsResponse, countResponse] = await Promise.all([
        adminApi.moderationSummary(),
        adminApi.flaggedEvidence(),
        adminApi.archivedEvidence(),
        adminApi.auditLogs(),
        notificationApi.unreadCount()
      ]);
      setSummary(summaryResponse);
      setFlagged(flaggedResponse);
      setArchived(archivedResponse);
      setAuditLogs(logsResponse);
      setUnreadCount(countResponse.unreadCount);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  async function createOrganisation(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      await organisationApi.create(organisationForm);
      setMessage("Organisation created.");
      await load();
    } catch (err) {
      setError(err);
    }
  }

  async function flagEvidence(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      await evidenceApi.flag(Number(evidenceId), { reason: flagReason });
      setMessage("Evidence flagged.");
      await load();
    } catch (err) {
      setError(err);
    }
  }

  async function createModerationNote(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      await adminApi.createModerationNote(Number(evidenceId), { details: noteDetails });
      setMessage("Moderation note created.");
      await load();
    } catch (err) {
      setError(err);
    }
  }

  async function recalculateScore() {
    setError(null);
    try {
      await levelPlayApi.recalculate(Number(recalculateAthleteProfileId));
      setMessage("LevelPlay score recalculated.");
      await load();
    } catch (err) {
      setError(err);
    }
  }

  return (
    <div className="page">
      <h1>Admin Workspace</h1>
      <p className="muted">Validate organisation creation, moderation basics, audit logs, and LevelPlay recalculation.</p>
      <div className="actions">
        <button type="button" onClick={load}>
          Refresh admin data
        </button>
      </div>
      {loading && <LoadingState />}
      {message && <div className="alert success">{message}</div>}
      <ApiErrorMessage error={error} />

      <div className="grid two">
        <section className="panel">
          <h2>Create Organisation</h2>
          <form className="form compact" onSubmit={createOrganisation}>
            <input value={organisationForm.name} onChange={(e) => setOrganisationForm({ ...organisationForm, name: e.target.value })} placeholder="Name" required />
            <input value={organisationForm.type} onChange={(e) => setOrganisationForm({ ...organisationForm, type: e.target.value })} placeholder="Type" required />
            <input value={organisationForm.location} onChange={(e) => setOrganisationForm({ ...organisationForm, location: e.target.value })} placeholder="Location" required />
            <input value={organisationForm.contactEmail ?? ""} onChange={(e) => setOrganisationForm({ ...organisationForm, contactEmail: e.target.value })} placeholder="Contact email" type="email" />
            <button type="submit">Create organisation</button>
          </form>
        </section>

        <section className="panel">
          <h2>Moderation Actions</h2>
          <form className="form compact" onSubmit={flagEvidence}>
            <input value={evidenceId} onChange={(e) => setEvidenceId(e.target.value)} placeholder="Evidence ID" />
            <textarea value={flagReason} onChange={(e) => setFlagReason(e.target.value)} placeholder="Flag reason" required />
            <button type="submit" disabled={!evidenceId}>
              Flag evidence
            </button>
          </form>
          <form className="form compact" onSubmit={createModerationNote}>
            <textarea value={noteDetails} onChange={(e) => setNoteDetails(e.target.value)} placeholder="Moderation note" required />
            <button type="submit" disabled={!evidenceId}>
              Add moderation note
            </button>
          </form>
        </section>

        <section className="panel">
          <h2>LevelPlay Admin</h2>
          <div className="inline-form">
            <input value={recalculateAthleteProfileId} onChange={(e) => setRecalculateAthleteProfileId(e.target.value)} placeholder="Athlete profile ID" />
            <button type="button" onClick={() => void recalculateScore()} disabled={!recalculateAthleteProfileId}>
              Recalculate score
            </button>
          </div>
          <p className="muted">Unread notifications: {unreadCount ?? "not loaded"}</p>
        </section>

        <section className="panel">
          <h2>Moderation Summary</h2>
          <DataBlock title="Summary" data={summary} />
        </section>

        <section className="panel">
          <h2>Flagged Evidence</h2>
          <DataBlock title="Flagged" data={flagged} />
        </section>

        <section className="panel">
          <h2>Archived Evidence</h2>
          <DataBlock title="Archived" data={archived} />
        </section>

        <section className="panel wide">
          <h2>Audit Logs</h2>
          <DataBlock title="Logs" data={auditLogs} />
        </section>
      </div>
    </div>
  );
}
