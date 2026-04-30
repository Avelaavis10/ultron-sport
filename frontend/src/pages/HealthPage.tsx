import { useEffect, useState } from "react";
import { healthApi, type HealthReadinessResponse, type HealthResponse, type HealthVersionResponse } from "../api/healthApi";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { DataBlock } from "../components/DataBlock";
import { LoadingState } from "../components/LoadingState";

export function HealthPage() {
  const [health, setHealth] = useState<HealthResponse | null>(null);
  const [readiness, setReadiness] = useState<HealthReadinessResponse | null>(null);
  const [version, setVersion] = useState<HealthVersionResponse | null>(null);
  const [error, setError] = useState<unknown>(null);
  const [loading, setLoading] = useState(true);

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [healthResponse, readinessResponse, versionResponse] = await Promise.all([
        healthApi.health(),
        healthApi.readiness(),
        healthApi.version()
      ]);
      setHealth(healthResponse);
      setReadiness(readinessResponse);
      setVersion(versionResponse);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  return (
    <div className="page">
      <section className="hero">
        <h1>Ultron Sport MVP Prototype</h1>
        <p>Lightweight browser client for validating the Spring Boot MVP API.</p>
      </section>
      <button type="button" onClick={load}>
        Refresh health
      </button>
      {loading && <LoadingState label="Checking backend" />}
      <ApiErrorMessage error={error} />
      <div className="grid three">
        <DataBlock title="Health" data={health} />
        <DataBlock title="Readiness" data={readiness} />
        <DataBlock title="Version" data={version} />
      </div>
    </div>
  );
}
