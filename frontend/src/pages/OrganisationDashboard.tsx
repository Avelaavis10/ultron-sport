import { FormEvent, useState } from "react";
import { discoveryApi } from "../api/discoveryApi";
import { organisationApi } from "../api/organisationApi";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { DataBlock } from "../components/DataBlock";
import type { AthleteDiscoveryCardResponse, EvidenceDiscoveryCardResponse, OrganisationResponse, PageResponse } from "../types/apiTypes";

export function OrganisationDashboard() {
  const [name, setName] = useState("");
  const [location, setLocation] = useState("");
  const [sport, setSport] = useState("Football");
  const [organisations, setOrganisations] = useState<PageResponse<OrganisationResponse> | null>(null);
  const [athletes, setAthletes] = useState<PageResponse<AthleteDiscoveryCardResponse> | null>(null);
  const [evidence, setEvidence] = useState<PageResponse<EvidenceDiscoveryCardResponse> | null>(null);
  const [error, setError] = useState<unknown>(null);

  async function searchOrganisations(event: FormEvent) {
    event.preventDefault();
    setError(null);
    try {
      setOrganisations(await organisationApi.search({ name, location, size: 20 }));
    } catch (err) {
      setError(err);
    }
  }

  async function searchDiscovery() {
    setError(null);
    try {
      const [athletePage, evidencePage] = await Promise.all([
        discoveryApi.searchAthletes({ sport, hasVerifiedEvidence: true, size: 20 }),
        discoveryApi.searchEvidence({ sport, verificationStatus: "VERIFIED", size: 20 })
      ]);
      setAthletes(athletePage);
      setEvidence(evidencePage);
    } catch (err) {
      setError(err);
    }
  }

  return (
    <div className="page">
      <h1>Organisation Workspace</h1>
      <p className="muted">Validate organisation lookup and verified-only discovery access.</p>
      <ApiErrorMessage error={error} />

      <div className="grid two">
        <section className="panel">
          <h2>Organisation Search</h2>
          <form className="form compact" onSubmit={searchOrganisations}>
            <input value={name} onChange={(e) => setName(e.target.value)} placeholder="Organisation name" />
            <input value={location} onChange={(e) => setLocation(e.target.value)} placeholder="Location" />
            <button type="submit">Search organisations</button>
          </form>
          <DataBlock title="Organisations" data={organisations} />
        </section>

        <section className="panel">
          <h2>Verified Discovery</h2>
          <div className="inline-form">
            <input value={sport} onChange={(e) => setSport(e.target.value)} placeholder="Sport" />
            <button type="button" onClick={() => void searchDiscovery()}>
              Search verified athletes/evidence
            </button>
          </div>
          <DataBlock title="Athletes" data={athletes} />
          <DataBlock title="Evidence" data={evidence} />
        </section>
      </div>
    </div>
  );
}
