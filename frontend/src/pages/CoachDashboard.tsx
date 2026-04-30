import { FormEvent, useEffect, useState } from "react";
import { coachProfileApi } from "../api/coachProfileApi";
import { evidenceApi } from "../api/evidenceApi";
import { notificationApi } from "../api/notificationApi";
import { organisationApi } from "../api/organisationApi";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { DataBlock } from "../components/DataBlock";
import { EmptyState } from "../components/EmptyState";
import { LoadingState } from "../components/LoadingState";
import { NotificationSection } from "../components/sections/NotificationSection";
import type {
  CoachProfileResponse,
  CreateCoachProfileRequest,
  EvidenceResponse,
  OrganisationResponse,
  PageResponse,
  VerificationContextResponse
} from "../types/apiTypes";

const defaultProfile: CreateCoachProfileRequest = {
  certificationReference: "MVP-COACH-001",
  organisationId: null,
  organisationName: "Ultron Football Academy",
  sport: "Football",
  qualificationSummary: "Grassroots coach profile created from the React MVP prototype.",
  yearsExperience: 4
};

function statusClass(status: string) {
  return status.toLowerCase().replace(/_/g, "-");
}

function evidenceLink(evidence: EvidenceResponse) {
  return evidence.externalVideoLink || evidence.fileUrl || "";
}

export function CoachDashboard() {
  const [profile, setProfile] = useState<CoachProfileResponse | null>(null);
  const [profileForm, setProfileForm] = useState<CreateCoachProfileRequest>(defaultProfile);
  const [organisationName, setOrganisationName] = useState("");
  const [organisationLocation, setOrganisationLocation] = useState("");
  const [organisations, setOrganisations] = useState<PageResponse<OrganisationResponse> | null>(null);
  const [pending, setPending] = useState<EvidenceResponse[]>([]);
  const [selectedEvidenceId, setSelectedEvidenceId] = useState("");
  const [rejectReason, setRejectReason] = useState("Evidence does not clearly show the claimed performance.");
  const [context, setContext] = useState<VerificationContextResponse | null>(null);
  const [unreadCount, setUnreadCount] = useState<number | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState("");
  const [message, setMessage] = useState("");
  const [notificationRefreshSignal, setNotificationRefreshSignal] = useState(0);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [profileResult, pendingResult, countResult, organisationsResult] = await Promise.allSettled([
        coachProfileApi.me(),
        evidenceApi.pendingVerification(),
        notificationApi.unreadCount(),
        organisationApi.search({ size: 10 })
      ]);

      if (profileResult.status === "fulfilled") {
        setProfile(profileResult.value);
        setProfileForm({
          certificationReference: profileResult.value.certificationReference,
          organisationId: profileResult.value.organisationId ?? null,
          organisationName: profileResult.value.organisationName ?? "",
          sport: profileResult.value.sport ?? "",
          qualificationSummary: profileResult.value.qualificationSummary ?? "",
          yearsExperience: profileResult.value.yearsExperience ?? 0
        });
      } else {
        setProfile(null);
      }

      if (pendingResult.status === "fulfilled") {
        setPending(pendingResult.value);
        if (!selectedEvidenceId && pendingResult.value.length > 0) {
          setSelectedEvidenceId(String(pendingResult.value[0].id));
        }
      }
      if (countResult.status === "fulfilled") setUnreadCount(countResult.value.unreadCount);
      if (organisationsResult.status === "fulfilled") setOrganisations(organisationsResult.value);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }

  async function afterAction(successMessage: string) {
    setMessage(successMessage);
    setNotificationRefreshSignal((value) => value + 1);
    await load();
  }

  useEffect(() => {
    void load();
  }, []);

  async function saveProfile(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setActionLoading("profile");
    try {
      const payload = {
        ...profileForm,
        organisationId: profileForm.organisationId ?? null,
        organisationName: profileForm.organisationName || null,
        sport: profileForm.sport || null,
        qualificationSummary: profileForm.qualificationSummary || null,
        yearsExperience: profileForm.yearsExperience ?? null
      };
      const saved = profile ? await coachProfileApi.updateMe(payload) : await coachProfileApi.create(payload);
      setProfile(saved);
      await afterAction(profile ? "Coach profile updated." : "Coach profile created. You can now verify evidence.");
    } catch (err) {
      setError(err);
    } finally {
      setActionLoading("");
    }
  }

  async function searchOrganisations(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setActionLoading("organisations");
    try {
      setOrganisations(await organisationApi.search({ name: organisationName, location: organisationLocation, size: 10 }));
    } catch (err) {
      setError(err);
    } finally {
      setActionLoading("");
    }
  }

  function useOrganisation(organisation: OrganisationResponse) {
    setProfileForm({
      ...profileForm,
      organisationId: organisation.id,
      organisationName: organisation.name
    });
    setMessage(`Selected organisation #${organisation.id}: ${organisation.name}`);
  }

  async function loadContext(evidenceId = selectedEvidenceId) {
    if (!evidenceId) return;
    setError(null);
    setActionLoading(`context-${evidenceId}`);
    try {
      setSelectedEvidenceId(String(evidenceId));
      setContext(await evidenceApi.verificationContext(Number(evidenceId)));
    } catch (err) {
      setError(err);
    } finally {
      setActionLoading("");
    }
  }

  async function verifyEvidence(evidenceId: number) {
    setError(null);
    setActionLoading(`verify-${evidenceId}`);
    try {
      await evidenceApi.verify(evidenceId);
      await afterAction("Evidence verified. The athlete will receive a notification and LevelPlay may update.");
      setContext(null);
    } catch (err) {
      setError(err);
    } finally {
      setActionLoading("");
    }
  }

  async function rejectEvidence(event: FormEvent) {
    event.preventDefault();
    if (!selectedEvidenceId) return;
    setError(null);
    setActionLoading("reject");
    try {
      await evidenceApi.reject(Number(selectedEvidenceId), { reason: rejectReason });
      await afterAction("Evidence rejected. The athlete will receive the reason.");
      setContext(null);
    } catch (err) {
      setError(err);
    } finally {
      setActionLoading("");
    }
  }

  return (
    <div className="page">
      <h1>Coach Workspace</h1>
      <p className="muted">Manual happy path: coach profile, pending evidence, context, verify/reject, notifications.</p>
      <div className="actions">
        <button type="button" onClick={() => void load()}>
          Refresh coach data
        </button>
      </div>
      {loading && <LoadingState />}
      {message && <div className="alert success">{message}</div>}
      <ApiErrorMessage error={error} />

      <div className="grid two">
        <section className="panel">
          <div className="section-heading">
            <div>
              <h2>1. Coach Profile</h2>
              <p className="muted">{profile ? `Coach profile #${profile.id}` : "A coach profile is required before verification."}</p>
            </div>
            {profile && <span className={`status-pill ${statusClass(profile.verificationStatus)}`}>{profile.verificationStatus}</span>}
          </div>
          <form className="form compact" onSubmit={saveProfile}>
            <input value={profileForm.certificationReference} onChange={(e) => setProfileForm({ ...profileForm, certificationReference: e.target.value })} placeholder="Certification reference" required />
            <input value={profileForm.organisationId ?? ""} onChange={(e) => setProfileForm({ ...profileForm, organisationId: e.target.value ? Number(e.target.value) : null })} placeholder="Organisation ID" type="number" />
            <input value={profileForm.organisationName ?? ""} onChange={(e) => setProfileForm({ ...profileForm, organisationName: e.target.value })} placeholder="Organisation name" />
            <input value={profileForm.sport ?? ""} onChange={(e) => setProfileForm({ ...profileForm, sport: e.target.value })} placeholder="Sport" />
            <input value={profileForm.yearsExperience ?? 0} onChange={(e) => setProfileForm({ ...profileForm, yearsExperience: Number(e.target.value) })} placeholder="Years experience" type="number" min="0" />
            <textarea value={profileForm.qualificationSummary ?? ""} onChange={(e) => setProfileForm({ ...profileForm, qualificationSummary: e.target.value })} placeholder="Qualification summary" />
            <button type="submit" disabled={actionLoading === "profile"}>
              {profile ? "Update coach profile" : "Create coach profile"}
            </button>
          </form>
        </section>

        <section className="panel">
          <h2>2. Link Organisation</h2>
          <form className="form compact" onSubmit={searchOrganisations}>
            <input value={organisationName} onChange={(e) => setOrganisationName(e.target.value)} placeholder="Organisation name" />
            <input value={organisationLocation} onChange={(e) => setOrganisationLocation(e.target.value)} placeholder="Location" />
            <button type="submit" disabled={actionLoading === "organisations"}>
              Search organisations
            </button>
          </form>
          {organisations?.content.length ? (
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
                  <button type="button" className="secondary" onClick={() => useOrganisation(organisation)}>
                    Use
                  </button>
                </article>
              ))}
            </div>
          ) : (
            <EmptyState title="No organisations loaded" detail="Search or enter an organisation ID manually." />
          )}
        </section>

        <section className="panel wide">
          <div className="section-heading">
            <div>
              <h2>3. Pending Verification Evidence</h2>
              <p className="muted">Select evidence, view context, then verify or reject.</p>
            </div>
            <span className="metric">{pending.length} pending</span>
          </div>
          {!profile && (
            <div className="alert error">
              Coach profile is required before verifying evidence.
            </div>
          )}
          {pending.length === 0 ? (
            <EmptyState title="No pending evidence" detail="Athlete submissions will appear here after they are submitted." />
          ) : (
            <div className="card-list evidence-list">
              {pending.map((item) => (
                <article key={item.id} className="workflow-card">
                  <div>
                    <div className="row-title">
                      <strong>#{item.id} {item.title}</strong>
                      <span className={`status-pill ${statusClass(item.verificationStatus)}`}>{item.verificationStatus}</span>
                    </div>
                    <p>{item.description || "No description"}</p>
                    <small className="muted">
                      Athlete profile #{item.athleteProfileId} - {item.sport} - {item.position} - {item.eventDate}
                    </small>
                    {evidenceLink(item) && (
                      <p>
                        <a href={evidenceLink(item)} target="_blank" rel="noreferrer">
                          Open evidence link
                        </a>
                      </p>
                    )}
                  </div>
                  <div className="button-row vertical">
                    <button type="button" className="secondary" onClick={() => void loadContext(String(item.id))} disabled={actionLoading === `context-${item.id}`}>
                      View context
                    </button>
                    <button type="button" onClick={() => void verifyEvidence(item.id)} disabled={!profile || actionLoading === `verify-${item.id}`}>
                      Verify
                    </button>
                    <button type="button" className="secondary" onClick={() => setSelectedEvidenceId(String(item.id))}>
                      Reject this
                    </button>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="panel">
          <h2>4. Verification Context</h2>
          <div className="inline-form">
            <input value={selectedEvidenceId} onChange={(e) => setSelectedEvidenceId(e.target.value)} placeholder="Evidence ID" />
            <button type="button" onClick={() => void loadContext()} disabled={!selectedEvidenceId}>
              Load context
            </button>
          </div>
          <DataBlock title="Context response" data={context} />
        </section>

        <section className="panel">
          <h2>5. Reject Evidence</h2>
          <form className="form compact" onSubmit={rejectEvidence}>
            <input value={selectedEvidenceId} onChange={(e) => setSelectedEvidenceId(e.target.value)} placeholder="Evidence ID" required />
            <textarea value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} placeholder="Reject reason" required />
            <button type="submit" disabled={!profile || !selectedEvidenceId || actionLoading === "reject"}>
              Reject evidence
            </button>
          </form>
          <p className="muted">Unread notifications: {unreadCount ?? "not loaded"}</p>
        </section>

        <NotificationSection title="6. Coach Notifications" refreshSignal={notificationRefreshSignal} />
      </div>
    </div>
  );
}
