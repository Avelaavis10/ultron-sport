import { FormEvent, useState } from "react";
import { discoveryApi } from "../api/discoveryApi";
import { levelPlayApi } from "../api/levelPlayApi";
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
  AthleteDiscoveryCardResponse,
  AthleteDiscoveryProfileResponse,
  EvidenceDiscoveryCardResponse,
  LevelPlayScoreExplanationResponse,
  LevelPlayScoreResponse,
  PageResponse
} from "../types/apiTypes";

type DiscoveryFilters = {
  keyword: string;
  sport: string;
  position: string;
  location: string;
  page: number;
  size: number;
};

const defaultFilters: DiscoveryFilters = {
  keyword: "",
  sport: "Football",
  position: "",
  location: "",
  page: 0,
  size: 20
};

export function ScoutDashboard() {
  const [athleteFilters, setAthleteFilters] = useState<DiscoveryFilters>(defaultFilters);
  const [evidenceFilters, setEvidenceFilters] = useState<DiscoveryFilters>(defaultFilters);
  const [athletes, setAthletes] = useState<PageResponse<AthleteDiscoveryCardResponse> | null>(null);
  const [evidence, setEvidence] = useState<PageResponse<EvidenceDiscoveryCardResponse> | null>(null);
  const [athleteProfileId, setAthleteProfileId] = useState("");
  const [profile, setProfile] = useState<AthleteDiscoveryProfileResponse | null>(null);
  const [score, setScore] = useState<LevelPlayScoreResponse | null>(null);
  const [explanation, setExplanation] = useState<LevelPlayScoreExplanationResponse | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState("");
  const [message, setMessage] = useState("");

  async function searchAthletes(event?: FormEvent) {
    event?.preventDefault();
    setError(null);
    setLoading("athletes");
    try {
      setAthletes(await discoveryApi.searchAthletes({ ...athleteFilters, hasVerifiedEvidence: true }));
      setMessage("Athlete discovery search complete.");
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  async function searchEvidence(event?: FormEvent) {
    event?.preventDefault();
    setError(null);
    setLoading("evidence");
    try {
      setEvidence(await discoveryApi.searchEvidence({ ...evidenceFilters, verificationStatus: "VERIFIED" }));
      setMessage("Verified evidence search complete.");
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  async function loadProfile(idValue = athleteProfileId) {
    if (!idValue) return;
    setError(null);
    setLoading("profile");
    try {
      const id = Number(idValue);
      const [profileResponse, scoreResponse, explanationResponse] = await Promise.all([
        discoveryApi.getAthleteProfile(id),
        levelPlayApi.getForAthlete(id),
        levelPlayApi.explain(id)
      ]);
      setAthleteProfileId(String(id));
      setProfile(profileResponse);
      setScore(scoreResponse);
      setExplanation(explanationResponse);
      setMessage(`Loaded athlete profile #${id}.`);
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  return (
    <div className="page">
      <PageHeader title="Scout Workspace" description="Search discovery-safe athletes, verified evidence, and transparent LevelPlay explanations." />
      <WorkflowHint
        steps={[
          "Search verified athletes by sport, position, location, or keyword.",
          "Open a discovery profile from a result.",
          "Review verified evidence and LevelPlay explanation.",
          "Use evidence search when you want to browse clips directly."
        ]}
      />
      <SuccessMessage message={message} />
      <ApiErrorMessage error={error} />

      <div className="grid two">
        <section className="panel">
          <h2>1. Athlete Discovery Search</h2>
          <form className="form compact" onSubmit={searchAthletes}>
            <FormField label="Keyword">
              <input value={athleteFilters.keyword} onChange={(e) => setAthleteFilters({ ...athleteFilters, keyword: e.target.value })} placeholder="Name, organisation, or evidence term" />
            </FormField>
            <FormField label="Sport">
              <input value={athleteFilters.sport} onChange={(e) => setAthleteFilters({ ...athleteFilters, sport: e.target.value })} placeholder="Football" />
            </FormField>
            <FormField label="Position">
              <input value={athleteFilters.position} onChange={(e) => setAthleteFilters({ ...athleteFilters, position: e.target.value })} placeholder="Forward" />
            </FormField>
            <FormField label="Location">
              <input value={athleteFilters.location} onChange={(e) => setAthleteFilters({ ...athleteFilters, location: e.target.value })} placeholder="Cape Town" />
            </FormField>
            <FormField label="Page">
              <input type="number" min="0" value={athleteFilters.page} onChange={(e) => setAthleteFilters({ ...athleteFilters, page: Number(e.target.value) })} />
            </FormField>
            <FormField label="Size" hint="Maximum backend size is 50.">
              <input type="number" min="1" max="50" value={athleteFilters.size} onChange={(e) => setAthleteFilters({ ...athleteFilters, size: Number(e.target.value) })} />
            </FormField>
            <button type="submit" disabled={loading === "athletes"}>
              Search athletes
            </button>
          </form>
          {loading === "athletes" && <LoadingState />}
          {!athletes ? null : athletes.content.length === 0 ? (
            <EmptyState title="No athletes found" detail="Try a broader sport, position, location, or keyword." />
          ) : (
            <div className="card-list">
              {athletes.content.map((athlete) => (
                <article key={athlete.athleteProfileId} className="workflow-card">
                  <div>
                    <div className="row-title">
                      <strong>{athlete.displayName}</strong>
                      <StatusPill value={athlete.levelPlayTier} />
                    </div>
                    <p>{athlete.sport} - {athlete.position} - {athlete.location}</p>
                    <small className="muted">
                      Verified evidence: {athlete.verifiedEvidenceCount} - LevelPlay: {athlete.levelPlayScore ?? "n/a"}
                    </small>
                    {athlete.latestVerifiedEvidenceTitle && <p className="muted">{athlete.latestVerifiedEvidenceTitle}</p>}
                  </div>
                  <button type="button" onClick={() => void loadProfile(String(athlete.athleteProfileId))}>
                    Open profile
                  </button>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="panel">
          <h2>2. Verified Evidence Search</h2>
          <form className="form compact" onSubmit={searchEvidence}>
            <FormField label="Keyword">
              <input value={evidenceFilters.keyword} onChange={(e) => setEvidenceFilters({ ...evidenceFilters, keyword: e.target.value })} placeholder="Evidence title or description" />
            </FormField>
            <FormField label="Sport">
              <input value={evidenceFilters.sport} onChange={(e) => setEvidenceFilters({ ...evidenceFilters, sport: e.target.value })} placeholder="Football" />
            </FormField>
            <FormField label="Position">
              <input value={evidenceFilters.position} onChange={(e) => setEvidenceFilters({ ...evidenceFilters, position: e.target.value })} placeholder="Forward" />
            </FormField>
            <FormField label="Location">
              <input value={evidenceFilters.location} onChange={(e) => setEvidenceFilters({ ...evidenceFilters, location: e.target.value })} placeholder="Cape Town" />
            </FormField>
            <FormField label="Page">
              <input type="number" min="0" value={evidenceFilters.page} onChange={(e) => setEvidenceFilters({ ...evidenceFilters, page: Number(e.target.value) })} />
            </FormField>
            <FormField label="Size" hint="Maximum backend size is 50.">
              <input type="number" min="1" max="50" value={evidenceFilters.size} onChange={(e) => setEvidenceFilters({ ...evidenceFilters, size: Number(e.target.value) })} />
            </FormField>
            <button type="submit" disabled={loading === "evidence"}>
              Search verified evidence
            </button>
          </form>
          {loading === "evidence" && <LoadingState />}
          {!evidence ? null : evidence.content.length === 0 ? (
            <EmptyState title="No verified evidence found" detail="Scouts only see evidence returned by backend role visibility." />
          ) : (
            <div className="card-list">
              {evidence.content.map((item) => (
                <article key={item.evidenceId} className="workflow-card">
                  <div>
                    <div className="row-title">
                      <strong>#{item.evidenceId} {item.title}</strong>
                      <StatusPill value={item.verificationStatus} />
                    </div>
                    <p>{item.athleteDisplayName} - {item.sport} - {item.position}</p>
                    <small className="muted">{item.eventType} - {item.matchOrTraining} - {item.eventDate}</small>
                    {item.mediaUrl && (
                      <p>
                        <a href={item.mediaUrl} target="_blank" rel="noreferrer">
                          Open evidence
                        </a>
                      </p>
                    )}
                  </div>
                  <button type="button" className="secondary" onClick={() => void loadProfile(String(item.athleteProfileId))}>
                    Athlete profile
                  </button>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="panel">
          <h2>3. Athlete Discovery Profile</h2>
          <div className="inline-form">
            <FormField label="Athlete profile ID">
              <input value={athleteProfileId} onChange={(e) => setAthleteProfileId(e.target.value)} placeholder="Use an ID from search results" />
            </FormField>
            <button type="button" onClick={() => void loadProfile()} disabled={!athleteProfileId || loading === "profile"}>
              Load profile
            </button>
          </div>
          {loading === "profile" && <LoadingState />}
          {profile ? (
            <>
              <div className="summary-grid spacer-top">
                <div className="stat-card">
                  <span>Athlete</span>
                  <strong>{profile.displayName}</strong>
                </div>
                <div className="stat-card">
                  <span>Evidence</span>
                  <strong>{profile.evidence.length}</strong>
                </div>
                <div className="stat-card">
                  <span>Achievements</span>
                  <strong>{profile.achievements.length}</strong>
                </div>
              </div>
              <DataBlock title="Discovery-safe profile" data={profile} />
            </>
          ) : (
            <EmptyState title="No athlete profile loaded" detail="Open a search result or enter an athlete profile ID." />
          )}
        </section>

        <section className="panel">
          <h2>4. LevelPlay Lookup</h2>
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
                  <span>Verified evidence</span>
                  <strong>{score.verifiedEvidenceCount}</strong>
                </div>
              </div>
              <DataBlock title="Score explanation" data={explanation} />
            </>
          ) : (
            <EmptyState title="No LevelPlay score loaded" detail="Load an athlete profile to view the score and explanation." />
          )}
        </section>

        <NotificationSection title="5. Scout Notifications" />
      </div>
    </div>
  );
}
