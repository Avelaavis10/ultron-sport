import { FormEvent, useEffect, useState } from "react";
import { achievementApi } from "../api/achievementApi";
import { athleteProfileApi } from "../api/athleteProfileApi";
import { evidenceApi } from "../api/evidenceApi";
import { levelPlayApi } from "../api/levelPlayApi";
import { mediaApi } from "../api/mediaApi";
import { notificationApi } from "../api/notificationApi";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { DataBlock } from "../components/DataBlock";
import { EmptyState } from "../components/EmptyState";
import { LoadingState } from "../components/LoadingState";
import type {
  AchievementResponse,
  AthleteProfileResponse,
  CreateAthleteProfileRequest,
  CreateEvidenceRequest,
  EvidenceResponse,
  LevelPlayScoreResponse
} from "../types/apiTypes";

const profileDefaults: CreateAthleteProfileRequest = {
  sport: "Football",
  position: "Forward",
  age: 19,
  gender: "Female",
  location: "Cape Town",
  schoolOrClub: "Ultron Football Academy",
  organisationId: null,
  bio: "Fast winger with verified match evidence."
};

export function AthleteDashboard() {
  const [profile, setProfile] = useState<AthleteProfileResponse | null>(null);
  const [profileForm, setProfileForm] = useState<CreateAthleteProfileRequest>(profileDefaults);
  const [achievements, setAchievements] = useState<AchievementResponse[]>([]);
  const [evidence, setEvidence] = useState<EvidenceResponse[]>([]);
  const [score, setScore] = useState<LevelPlayScoreResponse | null>(null);
  const [unreadCount, setUnreadCount] = useState<number | null>(null);
  const [achievementTitle, setAchievementTitle] = useState("Regional Top Scorer");
  const [achievementDate, setAchievementDate] = useState("2024-09-14");
  const [evidenceForm, setEvidenceForm] = useState<CreateEvidenceRequest>({
    athleteProfileId: 0,
    title: "Two goals against City FC",
    description: "Match clip with goals and pressing actions.",
    sport: "Football",
    position: "Forward",
    eventType: "League match",
    matchOrTraining: "MATCH",
    eventDate: "2024-09-21",
    fileUrl: null,
    externalVideoLink: "https://video.example/evidence/two-goals"
  });
  const [submitEvidenceId, setSubmitEvidenceId] = useState("");
  const [mediaFile, setMediaFile] = useState<File | null>(null);
  const [mediaEvidenceId, setMediaEvidenceId] = useState("");
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [profileResult, achievementsResult, evidenceResult, notificationsResult] = await Promise.allSettled([
        athleteProfileApi.me(),
        achievementApi.my(),
        evidenceApi.my(),
        notificationApi.unreadCount()
      ]);
      if (profileResult.status === "fulfilled") {
        setProfile(profileResult.value);
        setProfileForm({
          sport: profileResult.value.sport,
          position: profileResult.value.position,
          age: profileResult.value.age,
          gender: profileResult.value.gender,
          location: profileResult.value.location,
          schoolOrClub: profileResult.value.schoolOrClub,
          organisationId: profileResult.value.organisationId,
          bio: profileResult.value.bio
        });
        setEvidenceForm((current) => ({ ...current, athleteProfileId: profileResult.value.id }));
        const scoreResult = await levelPlayApi.me().catch(() => null);
        setScore(scoreResult);
      } else {
        setProfile(null);
      }
      if (achievementsResult.status === "fulfilled") setAchievements(achievementsResult.value);
      if (evidenceResult.status === "fulfilled") {
        setEvidence(evidenceResult.value);
        if (!submitEvidenceId && evidenceResult.value.length > 0) {
          setSubmitEvidenceId(String(evidenceResult.value[0].id));
        }
      }
      if (notificationsResult.status === "fulfilled") setUnreadCount(notificationsResult.value.unreadCount);
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
      const saved = profile
        ? await athleteProfileApi.updateMe(profileForm)
        : await athleteProfileApi.create(profileForm);
      setProfile(saved);
      setMessage("Profile saved.");
      await load();
    } catch (err) {
      setError(err);
    }
  }

  async function linkOrganisation(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      await athleteProfileApi.linkOrganisation({
        organisationId: profileForm.organisationId ?? null,
        schoolOrClub: profileForm.schoolOrClub
      });
      setMessage("Organisation link saved.");
      await load();
    } catch (err) {
      setError(err);
    }
  }

  async function createAchievement(event: FormEvent) {
    event.preventDefault();
    if (!profile) return;
    setError(null);
    try {
      await achievementApi.create({
        athleteProfileId: profile.id,
        title: achievementTitle,
        description: "Created from the React MVP prototype.",
        achievedAt: achievementDate || null
      });
      setMessage("Achievement created.");
      await load();
    } catch (err) {
      setError(err);
    }
  }

  async function createEvidence(event: FormEvent) {
    event.preventDefault();
    if (!profile) return;
    setError(null);
    try {
      const created = await evidenceApi.create({ ...evidenceForm, athleteProfileId: profile.id });
      setSubmitEvidenceId(String(created.id));
      setMessage(`Evidence created as ${created.verificationStatus}.`);
      await load();
    } catch (err) {
      setError(err);
    }
  }

  async function submitEvidence() {
    setError(null);
    try {
      await evidenceApi.submit(Number(submitEvidenceId));
      setMessage("Evidence submitted.");
      await load();
    } catch (err) {
      setError(err);
    }
  }

  async function uploadAndAttachMedia(event: FormEvent) {
    event.preventDefault();
    if (!profile || !mediaFile || !mediaEvidenceId) return;
    setError(null);
    try {
      const uploaded = await mediaApi.upload(profile.id, mediaFile);
      await evidenceApi.attachMedia(Number(mediaEvidenceId), uploaded.mediaId);
      setMessage("Media uploaded and attached.");
      await load();
    } catch (err) {
      setError(err);
    }
  }

  return (
    <div className="page">
      <h1>Athlete Workspace</h1>
      <p className="muted">Validate profile, achievements, evidence, media, LevelPlay, and notifications.</p>
      <div className="actions">
        <button type="button" onClick={load}>
          Refresh athlete data
        </button>
      </div>
      {loading && <LoadingState />}
      {message && <div className="alert success">{message}</div>}
      <ApiErrorMessage error={error} />

      <div className="grid two">
        <section className="panel">
          <h2>My Profile</h2>
          <form className="form compact" onSubmit={saveProfile}>
            <input placeholder="Sport" value={profileForm.sport} onChange={(e) => setProfileForm({ ...profileForm, sport: e.target.value })} required />
            <input placeholder="Position" value={profileForm.position} onChange={(e) => setProfileForm({ ...profileForm, position: e.target.value })} required />
            <input placeholder="Age" type="number" value={profileForm.age} onChange={(e) => setProfileForm({ ...profileForm, age: Number(e.target.value) })} required />
            <input placeholder="Gender" value={profileForm.gender ?? ""} onChange={(e) => setProfileForm({ ...profileForm, gender: e.target.value })} />
            <input placeholder="Location" value={profileForm.location} onChange={(e) => setProfileForm({ ...profileForm, location: e.target.value })} required />
            <input placeholder="School or club" value={profileForm.schoolOrClub ?? ""} onChange={(e) => setProfileForm({ ...profileForm, schoolOrClub: e.target.value })} />
            <input placeholder="Organisation ID" type="number" value={profileForm.organisationId ?? ""} onChange={(e) => setProfileForm({ ...profileForm, organisationId: e.target.value ? Number(e.target.value) : null })} />
            <textarea placeholder="Bio" value={profileForm.bio ?? ""} onChange={(e) => setProfileForm({ ...profileForm, bio: e.target.value })} />
            <button type="submit">{profile ? "Update profile" : "Create profile"}</button>
          </form>
          <form className="inline-form" onSubmit={linkOrganisation}>
            <button type="submit" disabled={!profile}>
              Link organisation/school
            </button>
          </form>
          <DataBlock title="Profile response" data={profile} />
        </section>

        <section className="panel">
          <h2>Achievements</h2>
          <form className="form compact" onSubmit={createAchievement}>
            <input value={achievementTitle} onChange={(e) => setAchievementTitle(e.target.value)} required />
            <input type="date" value={achievementDate} onChange={(e) => setAchievementDate(e.target.value)} />
            <button type="submit" disabled={!profile}>
              Create achievement
            </button>
          </form>
          {achievements.length === 0 ? <EmptyState title="No achievements yet" /> : <DataBlock title="My achievements" data={achievements} />}
        </section>

        <section className="panel">
          <h2>Evidence</h2>
          <form className="form compact" onSubmit={createEvidence}>
            <input placeholder="Title" value={evidenceForm.title} onChange={(e) => setEvidenceForm({ ...evidenceForm, title: e.target.value })} required />
            <input placeholder="Sport" value={evidenceForm.sport} onChange={(e) => setEvidenceForm({ ...evidenceForm, sport: e.target.value })} required />
            <input placeholder="Position" value={evidenceForm.position} onChange={(e) => setEvidenceForm({ ...evidenceForm, position: e.target.value })} required />
            <input placeholder="Event type" value={evidenceForm.eventType} onChange={(e) => setEvidenceForm({ ...evidenceForm, eventType: e.target.value })} required />
            <select value={evidenceForm.matchOrTraining} onChange={(e) => setEvidenceForm({ ...evidenceForm, matchOrTraining: e.target.value as "MATCH" | "TRAINING" })}>
              <option value="MATCH">MATCH</option>
              <option value="TRAINING">TRAINING</option>
            </select>
            <input type="date" value={evidenceForm.eventDate} onChange={(e) => setEvidenceForm({ ...evidenceForm, eventDate: e.target.value })} required />
            <input placeholder="External video link" value={evidenceForm.externalVideoLink ?? ""} onChange={(e) => setEvidenceForm({ ...evidenceForm, externalVideoLink: e.target.value })} />
            <button type="submit" disabled={!profile}>
              Create URL-only evidence
            </button>
          </form>
          <div className="inline-form">
            <input placeholder="Evidence ID" value={submitEvidenceId} onChange={(e) => setSubmitEvidenceId(e.target.value)} />
            <button type="button" onClick={() => void submitEvidence()} disabled={!submitEvidenceId}>
              Submit evidence
            </button>
          </div>
          {evidence.length === 0 ? <EmptyState title="No evidence yet" /> : <DataBlock title="My evidence" data={evidence} />}
        </section>

        <section className="panel">
          <h2>Media, LevelPlay, Notifications</h2>
          <form className="form compact" onSubmit={uploadAndAttachMedia}>
            <input placeholder="Evidence ID to attach media" value={mediaEvidenceId} onChange={(e) => setMediaEvidenceId(e.target.value)} />
            <input type="file" accept="video/mp4,video/quicktime,image/jpeg,image/png" onChange={(e) => setMediaFile(e.target.files?.[0] ?? null)} />
            <button type="submit" disabled={!profile || !mediaFile || !mediaEvidenceId}>
              Upload and attach media
            </button>
          </form>
          <DataBlock title="LevelPlay score" data={score} />
          <p className="muted">Unread notifications: {unreadCount ?? "not loaded"}</p>
        </section>
      </div>
    </div>
  );
}
