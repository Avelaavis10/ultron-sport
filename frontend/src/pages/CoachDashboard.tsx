import { FormEvent, useEffect, useState } from "react";
import { coachProfileApi } from "../api/coachProfileApi";
import { evidenceApi } from "../api/evidenceApi";
import { notificationApi } from "../api/notificationApi";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { DataBlock } from "../components/DataBlock";
import { EmptyState } from "../components/EmptyState";
import { LoadingState } from "../components/LoadingState";
import type { CoachProfileResponse, CreateCoachProfileRequest, EvidenceResponse, VerificationContextResponse } from "../types/apiTypes";

const defaultProfile: CreateCoachProfileRequest = {
  certificationReference: "MVP-COACH-001",
  organisationId: null,
  organisationName: "Ultron Football Academy",
  sport: "Football",
  qualificationSummary: "Grassroots coach profile created from the React MVP prototype.",
  yearsExperience: 4
};

export function CoachDashboard() {
  const [profile, setProfile] = useState<CoachProfileResponse | null>(null);
  const [profileForm, setProfileForm] = useState<CreateCoachProfileRequest>(defaultProfile);
  const [pending, setPending] = useState<EvidenceResponse[]>([]);
  const [selectedEvidenceId, setSelectedEvidenceId] = useState("");
  const [rejectReason, setRejectReason] = useState("Evidence does not clearly show the claimed performance.");
  const [context, setContext] = useState<VerificationContextResponse | null>(null);
  const [unreadCount, setUnreadCount] = useState<number | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [profileResult, pendingResult, countResult] = await Promise.allSettled([
        coachProfileApi.me(),
        evidenceApi.pendingVerification(),
        notificationApi.unreadCount()
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
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  async function saveProfile(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      const saved = profile ? await coachProfileApi.updateMe(profileForm) : await coachProfileApi.create(profileForm);
      setProfile(saved);
      setMessage("Coach profile saved.");
      await load();
    } catch (err) {
      setError(err);
    }
  }

  async function verifyEvidence() {
    setError(null);
    try {
      await evidenceApi.verify(Number(selectedEvidenceId));
      setMessage("Evidence verified.");
      await load();
    } catch (err) {
      setError(err);
    }
  }

  async function rejectEvidence(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      await evidenceApi.reject(Number(selectedEvidenceId), { reason: rejectReason });
      setMessage("Evidence rejected.");
      await load();
    } catch (err) {
      setError(err);
    }
  }

  async function loadContext() {
    setError(null);
    try {
      setContext(await evidenceApi.verificationContext(Number(selectedEvidenceId)));
    } catch (err) {
      setError(err);
    }
  }

  return (
    <div className="page">
      <h1>Coach Workspace</h1>
      <p className="muted">Validate coach profile setup, pending evidence, verify/reject actions, and verification context.</p>
      <div className="actions">
        <button type="button" onClick={load}>
          Refresh coach data
        </button>
      </div>
      {loading && <LoadingState />}
      {message && <div className="alert success">{message}</div>}
      <ApiErrorMessage error={error} />

      <div className="grid two">
        <section className="panel">
          <h2>My Coach Profile</h2>
          <form className="form compact" onSubmit={saveProfile}>
            <input value={profileForm.certificationReference} onChange={(e) => setProfileForm({ ...profileForm, certificationReference: e.target.value })} placeholder="Certification reference" required />
            <input value={profileForm.organisationId ?? ""} onChange={(e) => setProfileForm({ ...profileForm, organisationId: e.target.value ? Number(e.target.value) : null })} placeholder="Organisation ID" type="number" />
            <input value={profileForm.organisationName ?? ""} onChange={(e) => setProfileForm({ ...profileForm, organisationName: e.target.value })} placeholder="Organisation name" />
            <input value={profileForm.sport ?? ""} onChange={(e) => setProfileForm({ ...profileForm, sport: e.target.value })} placeholder="Sport" />
            <input value={profileForm.yearsExperience ?? 0} onChange={(e) => setProfileForm({ ...profileForm, yearsExperience: Number(e.target.value) })} placeholder="Years experience" type="number" min="0" />
            <textarea value={profileForm.qualificationSummary ?? ""} onChange={(e) => setProfileForm({ ...profileForm, qualificationSummary: e.target.value })} placeholder="Qualification summary" />
            <button type="submit">{profile ? "Update coach profile" : "Create coach profile"}</button>
          </form>
          <DataBlock title="Coach profile response" data={profile} />
        </section>

        <section className="panel">
          <h2>Pending Verification</h2>
          <div className="inline-form">
            <input value={selectedEvidenceId} onChange={(e) => setSelectedEvidenceId(e.target.value)} placeholder="Evidence ID" />
            <button type="button" onClick={() => void verifyEvidence()} disabled={!selectedEvidenceId}>
              Verify
            </button>
            <button type="button" onClick={() => void loadContext()} disabled={!selectedEvidenceId}>
              View context
            </button>
          </div>
          <form className="form compact" onSubmit={rejectEvidence}>
            <textarea value={rejectReason} onChange={(e) => setRejectReason(e.target.value)} placeholder="Reject reason" required />
            <button type="submit" disabled={!selectedEvidenceId}>
              Reject evidence
            </button>
          </form>
          {pending.length === 0 ? <EmptyState title="No pending evidence" /> : <DataBlock title="Pending evidence" data={pending} />}
        </section>

        <section className="panel">
          <h2>Verification Context</h2>
          <DataBlock title="Context response" data={context} />
        </section>

        <section className="panel">
          <h2>Notifications</h2>
          <p className="muted">Unread notifications: {unreadCount ?? "not loaded"}</p>
        </section>
      </div>
    </div>
  );
}
