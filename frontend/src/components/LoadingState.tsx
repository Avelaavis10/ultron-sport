export function LoadingState({ label = "Loading" }: { label?: string }) {
  return <div className="state muted">{label}...</div>;
}
