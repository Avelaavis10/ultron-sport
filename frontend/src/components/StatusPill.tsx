type StatusPillProps = {
  value: string | number | null | undefined;
  label?: string;
};

export function statusClassFor(value: string | number | null | undefined) {
  return String(value ?? "unknown").toLowerCase().replace(/_/g, "-");
}

export function StatusPill({ value, label }: StatusPillProps) {
  if (value === null || value === undefined || value === "") {
    return null;
  }

  return <span className={`status-pill ${statusClassFor(value)}`}>{label ?? value}</span>;
}
