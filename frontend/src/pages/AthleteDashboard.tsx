import { FormEvent, useEffect, useMemo, useState } from "react";
import { achievementApi } from "../api/achievementApi";
import { athleteProfileApi } from "../api/athleteProfileApi";
import { evidenceApi } from "../api/evidenceApi";
import { levelPlayApi } from "../api/levelPlayApi";
import { mediaApi } from "../api/mediaApi";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { DataBlock } from "../components/DataBlock";
import { EmptyState } from "../components/EmptyState";
import { LoadingState } from "../components/LoadingState";
import { NotificationSection } from "../components/sections/NotificationSection";
import type {
  AchievementResponse,
  AthleteProfileResponse,
  CreateAthleteProfileRequest,
  CreateEvidenceRequest,
  EvidenceResponse,
  LevelPlayScoreExplanationResponse,
  LevelPlayScoreResponse,
  UploadMediaResponse
} from "../types/apiTypes";

type AchievementFormState = {
  title: string;
  description: string;
  achievedAt: string;
};

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

const achievementDefaults: AchievementFormState = {
  title: "Regional Top Scorer",
  description: "Recognised for a strong regional tournament performance.",
  achievedAt: "2024-09-14"
};

const evidenceDefaults: CreateEvidenceRequest = {
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
};

function statusClass(status: string) {
  return status.toLowerCase().replace(/_/g, "-");
}

function playableUrl(evidence: EvidenceResponse) {
  return evidence.externalVideoLink || evidence.fileUrl || "";
}

export function AthleteDashboard() {
  const [profile, setProfile] = useState<AthleteProfileResponse | null>(null);
  const [profileForm, setProfileForm] = useState<CreateAthleteProfileRequest>(profileDefaults);
  const [achievements, setAchievements] = useState<AchievementResponse[]>([]);
  const [achievementForm, setAchievementForm] = useState<AchievementFormState>(achievementDefaults);
  const [editingAchievementId, setEditingAchievementId] = useState<number | null>(null);
  const [evidence, setEvidence] = useState<EvidenceResponse[]>([]);
  const [evidenceForm, setEvidenceForm] = useState<CreateEvidenceRequest>(evidenceDefaults);
  const [selectedEvidenceId, setSelectedEvidenceId] = useState("");
  const [mediaEvidenceId, setMediaEvidenceId] = useState("");
  const [mediaId, setMediaId] = useState("");
  const [mediaFile, setMediaFile] = useState<File | null>(null);
  const [uploadedMedia, setUploadedMedia] = useState<UploadMediaResponse | null>(null);
  const [score, setScore] = useState<LevelPlayScoreResponse | null>(null);
  const [scoreExplanation, setScoreExplanation] = useState<LevelPlayScoreExplanationResponse | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState("");
  const [message, setMessage] = useState("");
  const [notificationRefreshSignal, setNotificationRefreshSignal] = useState(0);

  const editableEvidence = useMemo(
    () => evidence.filter((item) => item.verificationStatus === "DRAFT" || item.verificationStatus === "REJECTED"),
    [evidence]
  );

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const profileResult = await athleteProfileApi.me().catch(() => null);
      setProfile(profileResult);

      if (!profileResult) {
        setAchievements([]);
        setEvidence([]);
        setScore(null);
        setScoreExplanation(null);
        return;
      }

      setProfileForm({
        sport: profileResult.sport,
        position: profileResult.position,
        age: profileResult.age,
        gender: profileResult.gender ?? "",
        location: profileResult.location,
        schoolOrClub: profileResult.schoolOrClub ?? "",
        organisationId: profileResult.organisationId ?? null,
        bio: profileResult.bio ?? ""
      });
      setEvidenceForm((current) => ({
        ...current,
        athleteProfileId: profileResult.id,
        sport: current.sport || profileResult.sport,
        position: current.position || profileResult.position
      }));

      const [achievementsResult, evidenceResult, scoreResult, explanationResult] = await Promise.allSettled([
        achievementApi.my(),
        evidenceApi.my(),
        levelPlayApi.me(),
        levelPlayApi.explain(profileResult.id)
      ]);

      if (achievementsResult.status === "fulfilled") setAchievements(achievementsResult.value);
      if (evidenceResult.status === "fulfilled") {
        setEvidence(evidenceResult.value);
        if (!selectedEvidenceId && evidenceResult.value.length > 0) {
          setSelectedEvidenceId(String(evidenceResult.value[0].id));
        }
        if (!mediaEvidenceId) {
          const firstEditable = evidenceResult.value.find((item) => item.verificationStatus === "DRAFT" || item.verificationStatus === "REJECTED");
          if (firstEditable) setMediaEvidenceId(String(firstEditable.id));
        }
      }
      if (scoreResult.status === "fulfilled") setScore(scoreResult.value);
      if (explanationResult.status === "fulfilled") setScoreExplanation(explanationResult.value);
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
        gender: profileForm.gender || null,
        schoolOrClub: profileForm.schoolOrClub || null,
        bio: profileForm.bio || null
      };
      const saved = profile ? await athleteProfileApi.updateMe(payload) : await athleteProfileApi.create(payload);
      setProfile(saved);
      await afterAction(profile ? "Profile updated and LevelPlay refreshed." : "Profile created.");
    } catch (err) {
      setError(err);
    } finally {
      setActionLoading("");
    }
  }

  async function linkOrganisation(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setActionLoading("organisation");
    try {
      await athleteProfileApi.linkOrganisation({
        organisationId: profileForm.organisationId ?? null,
        schoolOrClub: profileForm.schoolOrClub || null
      });
      await afterAction("Organisation or school/club link saved.");
    } catch (err) {
      setError(err);
    } finally {
      setActionLoading("");
    }
  }

  async function saveAchievement(event: FormEvent) {
    event.preventDefault();
    if (!profile) return;
    setError(null);
    setActionLoading("achievement");
    try {
      const payload = {
        title: achievementForm.title,
        description: achievementForm.description || null,
        achievedAt: achievementForm.achievedAt || null
      };
      if (editingAchievementId) {
        await achievementApi.update(editingAchievementId, payload);
        setEditingAchievementId(null);
        await afterAction("Achievement updated and LevelPlay refreshed.");
      } else {
        await achievementApi.create({ athleteProfileId: profile.id, ...payload });
        await afterAction("Achievement created and LevelPlay refreshed.");
      }
      setAchievementForm(achievementDefaults);
    } catch (err) {
      setError(err);
    } finally {
      setActionLoading("");
    }
  }

  function editAchievement(achievement: AchievementResponse) {
    setEditingAchievementId(achievement.id);
    setAchievementForm({
      title: achievement.title,
      description: achievement.description ?? "",
      achievedAt: achievement.achievedAt ?? ""
    });
  }

  async function createEvidence(event: FormEvent) {
    event.preventDefault();
    if (!profile) return;
    setError(null);
    setActionLoading("evidence");
    try {
      const created = await evidenceApi.create({
        ...evidenceForm,
        athleteProfileId: profile.id,
        fileUrl: evidenceForm.fileUrl || null,
        externalVideoLink: evidenceForm.externalVideoLink || null
      });
      setSelectedEvidenceId(String(created.id));
      setMediaEvidenceId(String(created.id));
      await afterAction(`Evidence created as ${created.verificationStatus}.`);
    } catch (err) {
      setError(err);
    } finally {
      setActionLoading("");
    }
  }

  async function uploadMedia(event: FormEvent) {
    event.preventDefault();
    if (!profile || !mediaFile) return;
    setError(null);
    setActionLoading("media-upload");
    try {
      const uploaded = await mediaApi.upload(profile.id, mediaFile);
      setUploadedMedia(uploaded);
      setMediaId(String(uploaded.mediaId));
      await afterAction("Media uploaded. Attach it to editable evidence when ready.");
    } catch (err) {
      setError(err);
    } finally {
      setActionLoading("");
    }
  }

  async function attachMedia(event: FormEvent) {
    event.preventDefault();
    if (!mediaEvidenceId || !mediaId) return;
    setError(null);
    setActionLoading("media-attach");
    try {
      await evidenceApi.attachMedia(Number(mediaEvidenceId), Number(mediaId));
      await afterAction("Media attached to evidence.");
    } catch (err) {
      setError(err);
    } finally {
      setActionLoading("");
    }
  }

  async function submitEvidence(evidenceId: number) {
    setError(null);
    setActionLoading(`submit-${evidenceId}`);
    try {
      await evidenceApi.submit(evidenceId);
      await afterAction("Evidence submitted for coach verification.");
    } catch (err) {
      setError(err);
    } finally {
      setActionLoading("");
    }
  }

  return (
    <div className="page">
      <h1>Athlete Workspace</h1>
      <p className="muted">Manual happy path: profile, achievement, evidence, media, submit, LevelPlay, notifications.</p>
      <div className="actions">
        <button type="button" onClick={() => void load()}>
          Refresh athlete data
        </button>
      </div>
      {loading && <LoadingState />}
      {message && <div className="alert success">{message}</div>}
      <ApiErrorMessage error={error} />

      <div className="grid two">
        <section className="panel">
          <div className="section-heading">
            <div>
              <h2>1. Athlete Profile</h2>
              <p className="muted">{profile ? `Profile #${profile.id}` : "Create a profile before using evidence or achievements."}</p>
            </div>
            {profile?.profileCompletenessScore != null && (
              <span className="metric">{profile.profileCompletenessScore}% complete</span>
            )}
          </div>
          <form className="form compact" onSubmit={saveProfile}>
            <input placeholder="Sport" value={profileForm.sport} onChange={(e) => setProfileForm({ ...profileForm, sport: e.target.value })} required />
            <input placeholder="Position" value={profileForm.position} onChange={(e) => setProfileForm({ ...profileForm, position: e.target.value })} required />
            <input placeholder="Age" type="number" min="5" max="80" value={profileForm.age} onChange={(e) => setProfileForm({ ...profileForm, age: Number(e.target.value) })} required />
            <input placeholder="Gender" value={profileForm.gender ?? ""} onChange={(e) => setProfileForm({ ...profileForm, gender: e.target.value })} />
            <input placeholder="Location" value={profileForm.location} onChange={(e) => setProfileForm({ ...profileForm, location: e.target.value })} required />
            <input placeholder="School or club" value={profileForm.schoolOrClub ?? ""} onChange={(e) => setProfileForm({ ...profileForm, schoolOrClub: e.target.value })} />
            <input placeholder="Organisation ID" type="number" value={profileForm.organisationId ?? ""} onChange={(e) => setProfileForm({ ...profileForm, organisationId: e.target.value ? Number(e.target.value) : null })} />
            <textarea placeholder="Bio" value={profileForm.bio ?? ""} onChange={(e) => setProfileForm({ ...profileForm, bio: e.target.value })} />
            <button type="submit" disabled={actionLoading === "profile"}>
              {profile ? "Update profile" : "Create profile"}
            </button>
          </form>
          <form className="inline-form spacer-top" onSubmit={linkOrganisation}>
            <button type="submit" className="secondary" disabled={!profile || actionLoading === "organisation"}>
              Save organisation/school link
            </button>
          </form>
        </section>

        <section className="panel">
          <h2>2. Achievements</h2>
          <form className="form compact" onSubmit={saveAchievement}>
            <input placeholder="Achievement title" value={achievementForm.title} onChange={(e) => setAchievementForm({ ...achievementForm, title: e.target.value })} required />
            <input type="date" value={achievementForm.achievedAt} onChange={(e) => setAchievementForm({ ...achievementForm, achievedAt: e.target.value })} />
            <textarea placeholder="Description" value={achievementForm.description} onChange={(e) => setAchievementForm({ ...achievementForm, description: e.target.value })} />
            <button type="submit" disabled={!profile || actionLoading === "achievement"}>
              {editingAchievementId ? "Update achievement" : "Create achievement"}
            </button>
            {editingAchievementId && (
              <button type="button" className="secondary" onClick={() => { setEditingAchievementId(null); setAchievementForm(achievementDefaults); }}>
                Cancel edit
              </button>
            )}
          </form>
          {achievements.length === 0 ? (
            <EmptyState title="No achievements yet" detail="Add one to improve profile completeness and LevelPlay inputs." />
          ) : (
            <div className="card-list">
              {achievements.map((achievement) => (
                <article key={achievement.id} className="workflow-card">
                  <div>
                    <div className="row-title">
                      <strong>{achievement.title}</strong>
                      <span className="status-pill">{achievement.verified ? "VERIFIED" : "UNVERIFIED"}</span>
                    </div>
                    <p>{achievement.description || "No description"}</p>
                    <small className="muted">Date: {achievement.achievedAt || "not set"}</small>
                  </div>
                  <button type="button" className="secondary" onClick={() => editAchievement(achievement)}>
                    Edit
                  </button>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="panel wide">
          <h2>3. Evidence</h2>
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
            <input placeholder="File URL" value={evidenceForm.fileUrl ?? ""} onChange={(e) => setEvidenceForm({ ...evidenceForm, fileUrl: e.target.value })} />
            <input placeholder="External video link" value={evidenceForm.externalVideoLink ?? ""} onChange={(e) => setEvidenceForm({ ...evidenceForm, externalVideoLink: e.target.value })} />
            <textarea placeholder="Description" value={evidenceForm.description ?? ""} onChange={(e) => setEvidenceForm({ ...evidenceForm, description: e.target.value })} />
            <button type="submit" disabled={!profile || actionLoading === "evidence"}>
              Create URL-only evidence
            </button>
          </form>

          {evidence.length === 0 ? (
            <EmptyState title="No evidence yet" detail="Create DRAFT evidence, optionally attach media, then submit it." />
          ) : (
            <div className="card-list evidence-list">
              {evidence.map((item) => (
                <article key={item.id} className="workflow-card">
                  <div>
                    <div className="row-title">
                      <strong>#{item.id} {item.title}</strong>
                      <span className={`status-pill ${statusClass(item.verificationStatus)}`}>{item.verificationStatus}</span>
                    </div>
                    <p>{item.description || "No description"}</p>
                    <small className="muted">
                      {item.sport} - {item.position} - {item.eventType} - {item.eventDate}
                    </small>
                    {playableUrl(item) && (
                      <p>
                        <a href={playableUrl(item)} target="_blank" rel="noreferrer">
                          Open evidence link
                        </a>
                      </p>
                    )}
                    {item.mediaAssetId && <small className="muted">Media asset: #{item.mediaAssetId}</small>}
                  </div>
                  <div className="button-row vertical">
                    {(item.verificationStatus === "DRAFT" || item.verificationStatus === "REJECTED") && (
                      <>
                        <button type="button" onClick={() => void submitEvidence(item.id)} disabled={actionLoading === `submit-${item.id}`}>
                          Submit
                        </button>
                        <button type="button" className="secondary" onClick={() => setMediaEvidenceId(String(item.id))}>
                          Use for media
                        </button>
                      </>
                    )}
                    <button type="button" className="secondary" onClick={() => setSelectedEvidenceId(String(item.id))}>
                      Select #{item.id}
                    </button>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="panel">
          <h2>4. Media Upload And Attach</h2>
          <form className="form compact" onSubmit={uploadMedia}>
            <input type="file" accept="video/mp4,video/quicktime,image/jpeg,image/png" onChange={(e) => setMediaFile(e.target.files?.[0] ?? null)} />
            <button type="submit" disabled={!profile || !mediaFile || actionLoading === "media-upload"}>
              Upload media
            </button>
          </form>
          <form className="form compact spacer-top" onSubmit={attachMedia}>
            <select value={mediaEvidenceId} onChange={(e) => setMediaEvidenceId(e.target.value)}>
              <option value="">Select editable evidence</option>
              {editableEvidence.map((item) => (
                <option key={item.id} value={item.id}>
                  #{item.id} {item.title}
                </option>
              ))}
            </select>
            <input placeholder="Media ID" value={mediaId} onChange={(e) => setMediaId(e.target.value)} />
            <button type="submit" disabled={!mediaEvidenceId || !mediaId || actionLoading === "media-attach"}>
              Attach media to evidence
            </button>
          </form>
          <DataBlock title="Latest upload" data={uploadedMedia} />
        </section>

        <section className="panel">
          <h2>5. LevelPlay</h2>
          {score ? (
            <>
              <div className="summary-grid">
                <div className="stat-card">
                  <span>Score</span>
                  <strong>{score.finalCredibilityScore}</strong>
                </div>
                <div className="stat-card">
                  <span>Tier</span>
                  <strong>{score.tier}</strong>
                </div>
                <div className="stat-card">
                  <span>Completeness</span>
                  <strong>{score.profileCompletenessScore}%</strong>
                </div>
              </div>
              <DataBlock title="LevelPlay explanation" data={scoreExplanation} />
            </>
          ) : (
            <EmptyState title="No LevelPlay score yet" detail="Create a profile first, then add evidence and achievements." />
          )}
        </section>

        <NotificationSection title="6. Athlete Notifications" refreshSignal={notificationRefreshSignal} />
      </div>
    </div>
  );
}
