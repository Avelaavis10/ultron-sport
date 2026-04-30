import { FormEvent, useEffect, useState } from "react";
import { adminApi } from "../api/adminApi";
import { evidenceApi } from "../api/evidenceApi";
import { levelPlayApi } from "../api/levelPlayApi";
import { organisationApi } from "../api/organisationApi";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { DataBlock } from "../components/DataBlock";
import { EmptyState } from "../components/EmptyState";
import { LoadingState } from "../components/LoadingState";
import { NotificationSection } from "../components/sections/NotificationSection";
import type {
  AdminActionLogResponse,
  AdminAuditLogQuery,
  CreateOrganisationRequest,
  EvidenceResponse,
  LevelPlayScoreResponse,
  ModerationSummaryResponse,
  OrganisationResponse,
  PageResponse,
  UpdateOrganisationRequest,
  VerificationStatus
} from "../types/apiTypes";

type OrganisationFilters = {
  name: string;
  type: string;
  location: string;
  verificationStatus: "" | VerificationStatus;
  page: number;
  size: number;
};

type AuditFilters = {
  actionType: string;
  targetType: string;
  targetId: string;
  adminUserId: string;
  page: number;
  size: number;
  sortBy: string;
  sortDirection: string;
};

const defaultOrganisation: CreateOrganisationRequest = {
  name: "Ultron Football Academy",
  type: "ACADEMY",
  location: "Cape Town",
  contactEmail: "admin@ultronsport.test",
  primaryAdminUserId: null
};

const defaultOrganisationFilters: OrganisationFilters = {
  name: "",
  type: "",
  location: "",
  verificationStatus: "",
  page: 0,
  size: 20
};

const defaultUpdateOrganisation: UpdateOrganisationRequest = {
  name: "",
  type: "",
  location: "",
  contactEmail: "",
  verificationStatus: "PENDING_VERIFICATION"
};

const defaultAuditFilters: AuditFilters = {
  actionType: "",
  targetType: "",
  targetId: "",
  adminUserId: "",
  page: 0,
  size: 20,
  sortBy: "createdAt",
  sortDirection: "DESC"
};

const verificationStatuses: Array<"" | VerificationStatus> = ["", "PENDING_VERIFICATION", "VERIFIED", "REJECTED"];

function statusClass(status: string) {
  return status.toLowerCase().replace(/_/g, "-");
}

function optionalNumber(value: string): number | null {
  return value.trim() ? Number(value) : null;
}

function toAuditQuery(filters: AuditFilters): AdminAuditLogQuery {
  return {
    actionType: filters.actionType || null,
    targetType: filters.targetType || null,
    targetId: optionalNumber(filters.targetId),
    adminUserId: optionalNumber(filters.adminUserId),
    page: filters.page,
    size: filters.size,
    sortBy: filters.sortBy,
    sortDirection: filters.sortDirection
  };
}

export function AdminDashboard() {
  const [organisationForm, setOrganisationForm] = useState<CreateOrganisationRequest>(defaultOrganisation);
  const [organisationFilters, setOrganisationFilters] = useState<OrganisationFilters>(defaultOrganisationFilters);
  const [organisations, setOrganisations] = useState<PageResponse<OrganisationResponse> | null>(null);
  const [selectedOrganisationId, setSelectedOrganisationId] = useState("");
  const [updateOrganisationForm, setUpdateOrganisationForm] =
    useState<UpdateOrganisationRequest>(defaultUpdateOrganisation);
  const [updatedOrganisation, setUpdatedOrganisation] = useState<OrganisationResponse | null>(null);
  const [summary, setSummary] = useState<ModerationSummaryResponse | null>(null);
  const [flagged, setFlagged] = useState<EvidenceResponse[]>([]);
  const [archived, setArchived] = useState<EvidenceResponse[]>([]);
  const [auditLogs, setAuditLogs] = useState<PageResponse<AdminActionLogResponse> | null>(null);
  const [targetLogs, setTargetLogs] = useState<AdminActionLogResponse[]>([]);
  const [auditFilters, setAuditFilters] = useState<AuditFilters>(defaultAuditFilters);
  const [targetType, setTargetType] = useState("EVIDENCE");
  const [targetId, setTargetId] = useState("");
  const [evidenceId, setEvidenceId] = useState("");
  const [flagReason, setFlagReason] = useState("Flagged during MVP prototype validation.");
  const [archiveEvidenceId, setArchiveEvidenceId] = useState("");
  const [noteReason, setNoteReason] = useState("Manual moderation review.");
  const [noteDetails, setNoteDetails] = useState("Internal moderation note from React prototype.");
  const [recalculateAthleteProfileId, setRecalculateAthleteProfileId] = useState("");
  const [recalculationResult, setRecalculationResult] = useState<LevelPlayScoreResponse | LevelPlayScoreResponse[] | null>(
    null
  );
  const [notificationRefreshSignal, setNotificationRefreshSignal] = useState(0);
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState("");
  const [message, setMessage] = useState("");

  async function loadModerationData() {
    setLoading("admin-data");
    setError(null);
    try {
      const [summaryResponse, flaggedResponse, archivedResponse, logsResponse] = await Promise.all([
        adminApi.moderationSummary(),
        adminApi.flaggedEvidence(),
        adminApi.archivedEvidence(),
        adminApi.auditLogs(toAuditQuery(auditFilters))
      ]);
      setSummary(summaryResponse);
      setFlagged(flaggedResponse);
      setArchived(archivedResponse);
      setAuditLogs(logsResponse);
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  useEffect(() => {
    void loadModerationData();
  }, []);

  async function createOrganisation(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setLoading("create-organisation");
    try {
      const organisation = await organisationApi.create(organisationForm);
      setMessage(`Organisation #${organisation.id} created.`);
      setSelectedOrganisationId(String(organisation.id));
      setUpdateOrganisationForm({
        name: organisation.name,
        type: organisation.type,
        location: organisation.location,
        contactEmail: organisation.contactEmail ?? "",
        verificationStatus: organisation.verificationStatus
      });
      await Promise.all([searchOrganisations(), loadModerationData()]);
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  async function searchOrganisations(event?: FormEvent) {
    event?.preventDefault();
    setError(null);
    setLoading("organisations");
    try {
      const page = await organisationApi.search({ ...organisationFilters });
      setOrganisations(page);
      setMessage("Organisation search complete.");
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  async function selectOrganisation(organisation: OrganisationResponse) {
    setSelectedOrganisationId(String(organisation.id));
    setUpdateOrganisationForm({
      name: organisation.name,
      type: organisation.type,
      location: organisation.location,
      contactEmail: organisation.contactEmail ?? "",
      verificationStatus: organisation.verificationStatus
    });
    setUpdatedOrganisation(organisation);
  }

  async function loadOrganisationForUpdate() {
    if (!selectedOrganisationId) return;
    setError(null);
    setLoading("organisation-detail");
    try {
      const organisation = await organisationApi.getById(Number(selectedOrganisationId));
      await selectOrganisation(organisation);
      setMessage(`Loaded organisation #${organisation.id}.`);
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  async function updateOrganisation(event: FormEvent) {
    event.preventDefault();
    if (!selectedOrganisationId) return;
    setError(null);
    setLoading("update-organisation");
    try {
      const organisation = await organisationApi.update(Number(selectedOrganisationId), updateOrganisationForm);
      await selectOrganisation(organisation);
      setMessage(`Organisation #${organisation.id} updated.`);
      await searchOrganisations();
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  async function flagEvidence(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setLoading("flag-evidence");
    try {
      await evidenceApi.flag(Number(evidenceId), { reason: flagReason });
      setMessage(`Evidence #${evidenceId} flagged.`);
      setNotificationRefreshSignal((value) => value + 1);
      await loadModerationData();
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  async function archiveEvidence(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setLoading("archive-evidence");
    try {
      await evidenceApi.archive(Number(archiveEvidenceId));
      setMessage(`Evidence #${archiveEvidenceId} archived.`);
      setNotificationRefreshSignal((value) => value + 1);
      await loadModerationData();
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  async function createModerationNote(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setLoading("moderation-note");
    try {
      await adminApi.createModerationNote(Number(evidenceId), { reason: noteReason, details: noteDetails });
      setMessage(`Moderation note added to evidence #${evidenceId}.`);
      await loadModerationData();
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  async function searchAuditLogs(event?: FormEvent) {
    event?.preventDefault();
    setError(null);
    setLoading("audit-logs");
    try {
      const page = await adminApi.auditLogs(toAuditQuery(auditFilters));
      setAuditLogs(page);
      setMessage("Audit log search complete.");
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  async function loadTargetLogs(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setLoading("target-logs");
    try {
      const logs = await adminApi.auditLogsForTarget(targetType, Number(targetId));
      setTargetLogs(logs);
      setMessage(`Loaded ${logs.length} audit log(s) for ${targetType} #${targetId}.`);
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  async function recalculateScore() {
    setError(null);
    setLoading("recalculate-one");
    try {
      const score = await levelPlayApi.recalculate(Number(recalculateAthleteProfileId));
      setRecalculationResult(score);
      setMessage(`LevelPlay score recalculated for athlete profile #${score.athleteProfileId}.`);
      await loadModerationData();
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  async function recalculateAllScores() {
    setError(null);
    setLoading("recalculate-all");
    try {
      const scores = await levelPlayApi.recalculateAll();
      setRecalculationResult(scores);
      setMessage(`Recalculated ${scores.length} LevelPlay score(s).`);
      await loadModerationData();
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  return (
    <div className="page">
      <h1>Admin Workspace</h1>
      <p className="muted">Validate organisation management, moderation, audit logs, and LevelPlay recalculation.</p>
      <div className="actions">
        <button type="button" onClick={() => void loadModerationData()} disabled={loading === "admin-data"}>
          Refresh admin data
        </button>
      </div>
      {loading === "admin-data" && <LoadingState />}
      {message && <div className="alert success">{message}</div>}
      <ApiErrorMessage error={error} />

      <div className="grid two">
        <section className="panel">
          <h2>1. Create Organisation</h2>
          <form className="form compact" onSubmit={createOrganisation}>
            <input value={organisationForm.name} onChange={(e) => setOrganisationForm({ ...organisationForm, name: e.target.value })} placeholder="Name" required />
            <input value={organisationForm.type} onChange={(e) => setOrganisationForm({ ...organisationForm, type: e.target.value })} placeholder="Type" required />
            <input value={organisationForm.location} onChange={(e) => setOrganisationForm({ ...organisationForm, location: e.target.value })} placeholder="Location" required />
            <input value={organisationForm.contactEmail ?? ""} onChange={(e) => setOrganisationForm({ ...organisationForm, contactEmail: e.target.value })} placeholder="Contact email" type="email" />
            <button type="submit" disabled={loading === "create-organisation"}>
              Create organisation
            </button>
          </form>
        </section>

        <section className="panel">
          <h2>2. Organisation Search / Update</h2>
          <form className="form compact" onSubmit={searchOrganisations}>
            <input value={organisationFilters.name} onChange={(e) => setOrganisationFilters({ ...organisationFilters, name: e.target.value })} placeholder="Name" />
            <input value={organisationFilters.type} onChange={(e) => setOrganisationFilters({ ...organisationFilters, type: e.target.value })} placeholder="Type" />
            <input value={organisationFilters.location} onChange={(e) => setOrganisationFilters({ ...organisationFilters, location: e.target.value })} placeholder="Location" />
            <select value={organisationFilters.verificationStatus} onChange={(e) => setOrganisationFilters({ ...organisationFilters, verificationStatus: e.target.value as "" | VerificationStatus })}>
              {verificationStatuses.map((status) => (
                <option key={status || "ANY"} value={status}>
                  {status || "Any status"}
                </option>
              ))}
            </select>
            <input type="number" min="0" value={organisationFilters.page} onChange={(e) => setOrganisationFilters({ ...organisationFilters, page: Number(e.target.value) })} placeholder="Page" />
            <input type="number" min="1" max="50" value={organisationFilters.size} onChange={(e) => setOrganisationFilters({ ...organisationFilters, size: Number(e.target.value) })} placeholder="Size" />
            <button type="submit" disabled={loading === "organisations"}>
              Search organisations
            </button>
          </form>
          {loading === "organisations" && <LoadingState />}
          {!organisations ? null : organisations.content.length === 0 ? (
            <EmptyState title="No organisations found" detail="Create one above, or broaden the filters." />
          ) : (
            <div className="card-list compact-list">
              {organisations.content.map((organisation) => (
                <article key={organisation.id} className="workflow-card">
                  <div>
                    <div className="row-title">
                      <strong>#{organisation.id} {organisation.name}</strong>
                      <span className={`status-pill ${statusClass(organisation.verificationStatus)}`}>{organisation.verificationStatus}</span>
                    </div>
                    <small className="muted">{organisation.type} - {organisation.location}</small>
                  </div>
                  <button type="button" className="secondary" onClick={() => void selectOrganisation(organisation)}>
                    Edit
                  </button>
                </article>
              ))}
            </div>
          )}

          <form className="form compact spacer-top" onSubmit={updateOrganisation}>
            <input value={selectedOrganisationId} onChange={(e) => setSelectedOrganisationId(e.target.value)} placeholder="Organisation ID" />
            <button type="button" className="secondary" onClick={() => void loadOrganisationForUpdate()} disabled={!selectedOrganisationId || loading === "organisation-detail"}>
              Load
            </button>
            <input value={updateOrganisationForm.name ?? ""} onChange={(e) => setUpdateOrganisationForm({ ...updateOrganisationForm, name: e.target.value })} placeholder="Updated name" />
            <input value={updateOrganisationForm.type ?? ""} onChange={(e) => setUpdateOrganisationForm({ ...updateOrganisationForm, type: e.target.value })} placeholder="Updated type" />
            <input value={updateOrganisationForm.location ?? ""} onChange={(e) => setUpdateOrganisationForm({ ...updateOrganisationForm, location: e.target.value })} placeholder="Updated location" />
            <input value={updateOrganisationForm.contactEmail ?? ""} onChange={(e) => setUpdateOrganisationForm({ ...updateOrganisationForm, contactEmail: e.target.value })} placeholder="Updated email" />
            <select value={updateOrganisationForm.verificationStatus ?? "PENDING_VERIFICATION"} onChange={(e) => setUpdateOrganisationForm({ ...updateOrganisationForm, verificationStatus: e.target.value as VerificationStatus })}>
              {verificationStatuses.filter(Boolean).map((status) => (
                <option key={status} value={status}>
                  {status}
                </option>
              ))}
            </select>
            <button type="submit" disabled={!selectedOrganisationId || loading === "update-organisation"}>
              Update organisation
            </button>
          </form>
          <DataBlock title="Last updated organisation" data={updatedOrganisation} />
        </section>

        <section className="panel">
          <h2>3. Moderation Summary</h2>
          {summary ? (
            <div className="summary-grid">
              <div className="stat-card"><span>Flagged</span><strong>{summary.flaggedEvidenceCount}</strong></div>
              <div className="stat-card"><span>Archived</span><strong>{summary.archivedEvidenceCount}</strong></div>
              <div className="stat-card"><span>Pending</span><strong>{summary.pendingVerificationCount}</strong></div>
              <div className="stat-card"><span>Verified</span><strong>{summary.verifiedEvidenceCount}</strong></div>
              <div className="stat-card"><span>Rejected</span><strong>{summary.rejectedEvidenceCount}</strong></div>
            </div>
          ) : (
            <EmptyState title="Summary not loaded" detail="Refresh admin data to load moderation counts." />
          )}
        </section>

        <section className="panel">
          <h2>4. Moderation Actions</h2>
          <form className="form compact" onSubmit={flagEvidence}>
            <input value={evidenceId} onChange={(e) => setEvidenceId(e.target.value)} placeholder="Evidence ID" required />
            <textarea value={flagReason} onChange={(e) => setFlagReason(e.target.value)} placeholder="Flag reason" required />
            <button type="submit" disabled={!evidenceId || loading === "flag-evidence"}>
              Flag evidence
            </button>
          </form>
          <form className="form compact spacer-top" onSubmit={archiveEvidence}>
            <input value={archiveEvidenceId} onChange={(e) => setArchiveEvidenceId(e.target.value)} placeholder="Evidence ID to archive" required />
            <button type="submit" disabled={!archiveEvidenceId || loading === "archive-evidence"}>
              Archive evidence
            </button>
          </form>
          <form className="form compact spacer-top" onSubmit={createModerationNote}>
            <input value={evidenceId} onChange={(e) => setEvidenceId(e.target.value)} placeholder="Evidence ID" required />
            <input value={noteReason} onChange={(e) => setNoteReason(e.target.value)} placeholder="Optional note reason" />
            <textarea value={noteDetails} onChange={(e) => setNoteDetails(e.target.value)} placeholder="Moderation note" required />
            <button type="submit" disabled={!evidenceId || loading === "moderation-note"}>
              Add moderation note
            </button>
          </form>
        </section>

        <section className="panel">
          <h2>5. Flagged Evidence</h2>
          {flagged.length === 0 ? (
            <EmptyState title="No flagged evidence" detail="Flagged evidence appears here after admin moderation." />
          ) : (
            <div className="card-list compact-list">
              {flagged.map((item) => (
                <article key={item.id} className="workflow-card">
                  <div>
                    <div className="row-title">
                      <strong>#{item.id} {item.title}</strong>
                      <span className={`status-pill ${statusClass(item.verificationStatus)}`}>{item.verificationStatus}</span>
                    </div>
                    <p>{item.sport} - {item.position} - athlete #{item.athleteProfileId}</p>
                    <small className="muted">{item.eventDate} - {item.eventType}</small>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="panel">
          <h2>6. Archived Evidence</h2>
          {archived.length === 0 ? (
            <EmptyState title="No archived evidence" detail="Archived evidence appears here after admin action." />
          ) : (
            <div className="card-list compact-list">
              {archived.map((item) => (
                <article key={item.id} className="workflow-card">
                  <div>
                    <div className="row-title">
                      <strong>#{item.id} {item.title}</strong>
                      <span className={`status-pill ${statusClass(item.verificationStatus)}`}>{item.verificationStatus}</span>
                    </div>
                    <p>{item.sport} - {item.position} - athlete #{item.athleteProfileId}</p>
                    <small className="muted">{item.updatedAt}</small>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="panel">
          <h2>7. Audit Log Search</h2>
          <form className="form compact" onSubmit={searchAuditLogs}>
            <input value={auditFilters.actionType} onChange={(e) => setAuditFilters({ ...auditFilters, actionType: e.target.value })} placeholder="Action type" />
            <input value={auditFilters.targetType} onChange={(e) => setAuditFilters({ ...auditFilters, targetType: e.target.value })} placeholder="Target type" />
            <input value={auditFilters.targetId} onChange={(e) => setAuditFilters({ ...auditFilters, targetId: e.target.value })} placeholder="Target ID" />
            <input value={auditFilters.adminUserId} onChange={(e) => setAuditFilters({ ...auditFilters, adminUserId: e.target.value })} placeholder="Admin user ID" />
            <input type="number" min="0" value={auditFilters.page} onChange={(e) => setAuditFilters({ ...auditFilters, page: Number(e.target.value) })} placeholder="Page" />
            <input type="number" min="1" max="50" value={auditFilters.size} onChange={(e) => setAuditFilters({ ...auditFilters, size: Number(e.target.value) })} placeholder="Size" />
            <select value={auditFilters.sortDirection} onChange={(e) => setAuditFilters({ ...auditFilters, sortDirection: e.target.value })}>
              <option value="DESC">Newest first</option>
              <option value="ASC">Oldest first</option>
            </select>
            <button type="submit" disabled={loading === "audit-logs"}>
              Search audit logs
            </button>
          </form>
          {loading === "audit-logs" && <LoadingState />}
          {!auditLogs ? null : auditLogs.content.length === 0 ? (
            <EmptyState title="No audit logs found" detail="Try broader filters or create an admin action first." />
          ) : (
            <div className="card-list compact-list">
              {auditLogs.content.map((log) => (
                <article key={log.id} className="workflow-card">
                  <div>
                    <div className="row-title">
                      <strong>#{log.id} {log.actionType}</strong>
                      <span className="status-pill">{log.targetType} #{log.targetId}</span>
                    </div>
                    <p>{log.reason || log.details || "No reason/details provided."}</p>
                    <small className="muted">Admin #{log.adminUserId} - {log.createdAt}</small>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="panel">
          <h2>8. Audit Logs By Target</h2>
          <form className="form compact" onSubmit={loadTargetLogs}>
            <input value={targetType} onChange={(e) => setTargetType(e.target.value)} placeholder="Target type, e.g. EVIDENCE" required />
            <input value={targetId} onChange={(e) => setTargetId(e.target.value)} placeholder="Target ID" required />
            <button type="submit" disabled={!targetType || !targetId || loading === "target-logs"}>
              Load target logs
            </button>
          </form>
          {loading === "target-logs" && <LoadingState />}
          {targetLogs.length === 0 ? (
            <EmptyState title="No target logs loaded" detail="Enter a target type and ID to inspect moderation history." />
          ) : (
            <div className="card-list compact-list">
              {targetLogs.map((log) => (
                <article key={log.id} className="workflow-card">
                  <div>
                    <strong>{log.actionType}</strong>
                    <p>{log.reason || log.details || "No reason/details provided."}</p>
                    <small className="muted">{log.createdAt}</small>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="panel">
          <h2>9. LevelPlay Admin</h2>
          <div className="inline-form">
            <input value={recalculateAthleteProfileId} onChange={(e) => setRecalculateAthleteProfileId(e.target.value)} placeholder="Athlete profile ID" />
            <button type="button" onClick={() => void recalculateScore()} disabled={!recalculateAthleteProfileId || loading === "recalculate-one"}>
              Recalculate one
            </button>
            <button type="button" className="secondary" onClick={() => void recalculateAllScores()} disabled={loading === "recalculate-all"}>
              Recalculate all
            </button>
          </div>
          <DataBlock title="Last recalculation result" data={recalculationResult} />
        </section>

        <NotificationSection title="10. Admin Notifications" refreshSignal={notificationRefreshSignal} />
      </div>
    </div>
  );
}
