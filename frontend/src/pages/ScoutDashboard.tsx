import { FormEvent, useState } from "react";
import { discoveryApi } from "../api/discoveryApi";
import { levelPlayApi } from "../api/levelPlayApi";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { DataBlock } from "../components/DataBlock";
import type {
  AthleteDiscoveryCardResponse,
  AthleteDiscoveryProfileResponse,
  EvidenceDiscoveryCardResponse,
  LevelPlayScoreExplanationResponse,
  LevelPlayScoreResponse,
  PageResponse
} from "../types/apiTypes";

export function ScoutDashboard() {
  const [sport, setSport] = useState("Football");
  const [position, setPosition] = useState("");
  const [keyword, setKeyword] = useState("");
  const [athletes, setAthletes] = useState<PageResponse<AthleteDiscoveryCardResponse> | null>(null);
  const [evidence, setEvidence] = useState<PageResponse<EvidenceDiscoveryCardResponse> | null>(null);
  const [athleteProfileId, setAthleteProfileId] = useState("");
  const [profile, setProfile] = useState<AthleteDiscoveryProfileResponse | null>(null);
  const [score, setScore] = useState<LevelPlayScoreResponse | null>(null);
  const [explanation, setExplanation] = useState<LevelPlayScoreExplanationResponse | null>(null);
  const [error, setError] = useState<unknown>(null);

  async function search(event?: FormEvent) {
    event?.preventDefault();
    setError(null);
    try {
      const query = { sport, position, keyword, hasVerifiedEvidence: true, size: 20 };
      const [athletePage, evidencePage] = await Promise.all([
        discoveryApi.searchAthletes(query),
        discoveryApi.searchEvidence({ sport, position, keyword, verificationStatus: "VERIFIED", size: 20 })
      ]);
      setAthletes(athletePage);
      setEvidence(evidencePage);
    } catch (err) {
      setError(err);
    }
  }

  async function loadProfile() {
    setError(null);
    try {
      const id = Number(athleteProfileId);
      const [profileResponse, scoreResponse, explanationResponse] = await Promise.all([
        discoveryApi.getAthleteProfile(id),
        levelPlayApi.getForAthlete(id),
        levelPlayApi.explain(id)
      ]);
      setProfile(profileResponse);
      setScore(scoreResponse);
      setExplanation(explanationResponse);
    } catch (err) {
      setError(err);
    }
  }

  return (
    <div className="page">
      <h1>Scout Workspace</h1>
      <p className="muted">Search discovery-safe athlete profiles and verified evidence only.</p>
      <ApiErrorMessage error={error} />

      <section className="panel">
        <h2>Discovery Search</h2>
        <form className="inline-form" onSubmit={search}>
          <input value={sport} onChange={(e) => setSport(e.target.value)} placeholder="Sport" />
          <input value={position} onChange={(e) => setPosition(e.target.value)} placeholder="Position" />
          <input value={keyword} onChange={(e) => setKeyword(e.target.value)} placeholder="Keyword" />
          <button type="submit">Search</button>
        </form>
      </section>

      <div className="grid two">
        <section className="panel">
          <h2>Athlete Results</h2>
          <DataBlock title="Athletes" data={athletes} />
        </section>
        <section className="panel">
          <h2>Verified Evidence Results</h2>
          <DataBlock title="Evidence" data={evidence} />
        </section>
        <section className="panel">
          <h2>Athlete Discovery Profile</h2>
          <div className="inline-form">
            <input value={athleteProfileId} onChange={(e) => setAthleteProfileId(e.target.value)} placeholder="Athlete profile ID" />
            <button type="button" onClick={() => void loadProfile()} disabled={!athleteProfileId}>
              Load profile and score
            </button>
          </div>
          <DataBlock title="Profile" data={profile} />
        </section>
        <section className="panel">
          <h2>LevelPlay</h2>
          <DataBlock title="Score" data={score} />
          <DataBlock title="Explanation" data={explanation} />
        </section>
      </div>
    </div>
  );
}
