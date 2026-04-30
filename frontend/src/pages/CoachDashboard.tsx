import { FormEvent, useEffect, useState } from "react";
import { coachProfileApi } from "../api/coachProfileApi";
import { evidenceApi } from "../api/evidenceApi";
import { notificationApi } from "../api/notificationApi";
import { organisationApi } from "../api/organisationApi";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { DataBlock } from "../components/DataBlock";
import { EmptyState } from "../components/EmptyState";
import { FormField } from "../components/FormField";
import { LoadingState } from "../components/LoadingState";
import { PageHeader } from "../components/PageHeader";
import { NotificationSection } from "../components/sections/NotificationSection";
import { StatusPill } from "../components/StatusPill";
import { SuccessMessage } from "../components/SuccessMessage";
import { WorkflowHint } from "../components/WorkflowHint";
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
      <PageHeader
        title="Coach Workspace"
        description="Create a coach profile, inspect pending evidence context, then verify or reject submissions."
      >
        <button type="button" onClick={() => void load()}>
          Refresh coach data
        </button>
      </PageHeader>
      <WorkflowHint
        steps={[
          "Create or update your coach profile.",
          "Link an organisation if available.",
          "Open pending evidence and inspect verification context.",
          "Verify or reject evidence with a reason.",
          "Check notifications and ask the athlete to confirm LevelPlay changes."
        ]}
      />
      {loading && <LoadingState />}
      <SuccessMessage message={message} />
      <ApiErrorMessage error={error} />

      <div className="grid two">
        <section className="panel">
          <div className="section-heading">
            <div>
              <h2>1. Coach Profile</h2>
              <p className="muted">{profile ? `Coach profile #${profile.id}` : "A coach profile is required before verification."}</p>
            </div>
            {profile && <StatusPill value={profile.verificationStatus} />}
          </div>
          <form className="form compact" onSubmit={saveProfile}>
            <FormField label="Certification reference" required>
              <input value={profileForm.certificationReference} onChange={(e) => setProfileForm({ ...profileForm, certificationReference: e.target.value })} placeholder="MVP-COACH-001" required />
            </FormField>
            <FormField label="Organisation ID" hint="Optional, but it strengthens verification context.">
              <input value={profileForm.organisationId ?? ""} onChange={(e) => setProfileForm({ ...profileForm, organisationId: e.target.value ? Number(e.target.value) : null })} type="number" />
            </FormField>
            <FormField label="Organisation name">
              <input value={profileForm.organisationName ?? ""} onChange={(e) => setProfileForm({ ...profileForm, organisationName: e.target.value })} placeholder="Ultron Football Academy" />
            </FormField>
            <FormField label="Sport">
              <input value={profileForm.sport ?? ""} onChange={(e) => setProfileForm({ ...profileForm, sport: e.target.value })} placeholder="Football" />
            </FormField>
            <FormField label="Years experience">
              <input value={profileForm.yearsExperience ?? 0} onChange={(e) => setProfileForm({ ...profileForm, yearsExperience: Number(e.target.value) })} type="number" min="0" />
            </FormField>
            <FormField label="Qualification summary">
              <textarea value={profileForm.qualificationSummary ?? ""} onChange={(e) => setProfileForm({ ...profileForm, qualificationSummary: e.target.value })} placeholder="Coaching background and qualifications" />
            </FormField>
            <button type="submit" disabled={actionLoading === "profile"}>
              {profile ? "Update coach profile" : "Create coach profile"}
            </button>
          </form>
        </section>

        <section className="panel">
          <h2>2. Link Organisation</h2>
          <form className="form compact" onSubmit={searchOrganisations}>
            <FormField label="Organisation name">
              <input value={organisationName} onChange={(e) => setOrganisationName(e.target.value)} placeholder="Ultron Football Academy" />
            </FormField>
            <FormField label="Location">
              <input value={organisationLocation} onChange={(e) => setOrganisationLocation(e.target.value)} placeholder="Cape Town" />
            </FormField>
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
                      <StatusPill value={organisation.verificationStatus} />
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
                      <StatusPill value={item.verificationStatus} />
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
            <FormField label="Evidence ID">
              <input value={selectedEvidenceId} onChange={(e) => setSelectedEvidenceId(e.target.value)} placeholder="Use an ID from the pending list" />
            </FormField>
            <button type="button" onClick={() => void loadContext()} disabled={!selectedEvidenceId}>
              Load context
            </button>
          </div>
          <DataBlock title="Context response" data={context} />
        </section>

        <section className="panel">
          <h2>5. Reject Evidence</h2>
          <form className="form compact" onSubmit={rejectEvidence}>
            <FormField label="Evidence ID" required>
              <input value={selectedEvidenceId} onChange={(e) => setSelectedEvidenceId(e.target.value)} placeholder="Use an ID from the pending list" required />
            </FormField>
            <FormField label="Reject reason" required>
              <textarea value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} placeholder="Explain what needs to be fixed" required />
            </FormField>
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
