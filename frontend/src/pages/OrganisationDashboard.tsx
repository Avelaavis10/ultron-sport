import { FormEvent, useState } from "react";
import { discoveryApi } from "../api/discoveryApi";
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
  AthleteDiscoveryCardResponse,
  EvidenceDiscoveryCardResponse,
  OrganisationResponse,
  PageResponse,
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

type DiscoveryFilters = {
  keyword: string;
  sport: string;
  position: string;
  location: string;
  page: number;
  size: number;
};

const organisationDefaults: OrganisationFilters = {
  name: "",
  type: "",
  location: "",
  verificationStatus: "",
  page: 0,
  size: 20
};

const discoveryDefaults: DiscoveryFilters = {
  keyword: "",
  sport: "Football",
  position: "",
  location: "",
  page: 0,
  size: 20
};

const verificationStatuses: Array<"" | VerificationStatus> = ["", "PENDING_VERIFICATION", "VERIFIED", "REJECTED"];

export function OrganisationDashboard() {
  const [organisationFilters, setOrganisationFilters] = useState<OrganisationFilters>(organisationDefaults);
  const [athleteFilters, setAthleteFilters] = useState<DiscoveryFilters>(discoveryDefaults);
  const [evidenceFilters, setEvidenceFilters] = useState<DiscoveryFilters>(discoveryDefaults);
  const [organisations, setOrganisations] = useState<PageResponse<OrganisationResponse> | null>(null);
  const [selectedOrganisationId, setSelectedOrganisationId] = useState("");
  const [selectedOrganisation, setSelectedOrganisation] = useState<OrganisationResponse | null>(null);
  const [athletes, setAthletes] = useState<PageResponse<AthleteDiscoveryCardResponse> | null>(null);
  const [evidence, setEvidence] = useState<PageResponse<EvidenceDiscoveryCardResponse> | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState("");
  const [message, setMessage] = useState("");

  async function searchOrganisations(event?: FormEvent) {
    event?.preventDefault();
    setError(null);
    setLoading("organisations");
    try {
      setOrganisations(await organisationApi.search(organisationFilters));
      setMessage("Organisation search complete.");
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  async function loadOrganisation(idValue = selectedOrganisationId) {
    if (!idValue) return;
    setError(null);
    setLoading("organisation-detail");
    try {
      const organisation = await organisationApi.getById(Number(idValue));
      setSelectedOrganisationId(String(organisation.id));
      setSelectedOrganisation(organisation);
      setMessage(`Loaded organisation #${organisation.id}.`);
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  async function searchAthletes(event?: FormEvent) {
    event?.preventDefault();
    setError(null);
    setLoading("athletes");
    try {
      setAthletes(await discoveryApi.searchAthletes({ ...athleteFilters, hasVerifiedEvidence: true }));
      setMessage("Organisation athlete discovery search complete.");
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
      setMessage("Organisation verified evidence search complete.");
    } catch (err) {
      setError(err);
    } finally {
      setLoading("");
    }
  }

  return (
    <div className="page">
      <PageHeader title="Organisation Workspace" description="Find organisations and review discovery-safe verified athlete evidence." />
      <WorkflowHint
        steps={[
          "Search or load your organisation record.",
          "Search verified athlete discovery cards.",
          "Review verified evidence returned by the backend.",
          "Use notifications to confirm organisation-side events."
        ]}
      />
      <SuccessMessage message={message} />
      <ApiErrorMessage error={error} />

      <div className="grid two">
        <section className="panel">
          <h2>1. Organisation Search</h2>
          <form className="form compact" onSubmit={searchOrganisations}>
            <FormField label="Name">
              <input value={organisationFilters.name} onChange={(e) => setOrganisationFilters({ ...organisationFilters, name: e.target.value })} placeholder="Ultron Football Academy" />
            </FormField>
            <FormField label="Type">
              <input value={organisationFilters.type} onChange={(e) => setOrganisationFilters({ ...organisationFilters, type: e.target.value })} placeholder="ACADEMY" />
            </FormField>
            <FormField label="Location">
              <input value={organisationFilters.location} onChange={(e) => setOrganisationFilters({ ...organisationFilters, location: e.target.value })} placeholder="Cape Town" />
            </FormField>
            <FormField label="Verification status">
              <select value={organisationFilters.verificationStatus} onChange={(e) => setOrganisationFilters({ ...organisationFilters, verificationStatus: e.target.value as "" | VerificationStatus })}>
                {verificationStatuses.map((status) => (
                  <option key={status || "ANY"} value={status}>
                    {status || "Any status"}
                  </option>
                ))}
              </select>
            </FormField>
            <FormField label="Page">
              <input type="number" min="0" value={organisationFilters.page} onChange={(e) => setOrganisationFilters({ ...organisationFilters, page: Number(e.target.value) })} />
            </FormField>
            <FormField label="Size" hint="Maximum backend size is 50.">
              <input type="number" min="1" max="50" value={organisationFilters.size} onChange={(e) => setOrganisationFilters({ ...organisationFilters, size: Number(e.target.value) })} />
            </FormField>
            <button type="submit" disabled={loading === "organisations"}>
              Search organisations
            </button>
          </form>
          {loading === "organisations" && <LoadingState />}
          {!organisations ? null : organisations.content.length === 0 ? (
            <EmptyState title="No organisations found" detail="Try broader filters or ask an admin to create the organisation." />
          ) : (
            <div className="card-list">
              {organisations.content.map((organisation) => (
                <article key={organisation.id} className="workflow-card">
                  <div>
                    <div className="row-title">
                      <strong>#{organisation.id} {organisation.name}</strong>
                      <StatusPill value={organisation.verificationStatus} />
                    </div>
                    <small className="muted">{organisation.type} - {organisation.location}</small>
                  </div>
                  <button type="button" className="secondary" onClick={() => void loadOrganisation(String(organisation.id))}>
                    View
                  </button>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="panel">
          <h2>2. Organisation Detail</h2>
          <div className="inline-form">
            <FormField label="Organisation ID">
              <input value={selectedOrganisationId} onChange={(e) => setSelectedOrganisationId(e.target.value)} placeholder="Use an ID from search results" />
            </FormField>
            <button type="button" onClick={() => void loadOrganisation()} disabled={!selectedOrganisationId || loading === "organisation-detail"}>
              Load organisation
            </button>
          </div>
          {loading === "organisation-detail" && <LoadingState />}
          {selectedOrganisation ? (
            <DataBlock title="Organisation" data={selectedOrganisation} />
          ) : (
            <EmptyState title="No organisation selected" detail="Use the search results or enter an organisation ID." />
          )}
        </section>

        <section className="panel">
          <h2>3. Athlete Discovery</h2>
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
              Search verified athletes
            </button>
          </form>
          {loading === "athletes" && <LoadingState />}
          {!athletes ? null : athletes.content.length === 0 ? (
            <EmptyState title="No verified athletes found" detail="Organisation users see discovery-safe verified profiles only." />
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
                    <small className="muted">Verified evidence: {athlete.verifiedEvidenceCount}</small>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="panel">
          <h2>4. Verified Evidence</h2>
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
            <EmptyState title="No verified evidence found" detail="Draft and pending evidence are hidden from organisation discovery." />
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
                    <small className="muted">{item.eventDate} - {item.eventType}</small>
                    {item.mediaUrl && (
                      <p>
                        <a href={item.mediaUrl} target="_blank" rel="noreferrer">
                          Open evidence
                        </a>
                      </p>
                    )}
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        <NotificationSection title="5. Organisation Notifications" />
      </div>
    </div>
  );
}
