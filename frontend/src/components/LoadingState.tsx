export function LoadingState({ label = "Loading" }: { label?: string }) {
  return (
    <div className="state muted" role="status" aria-live="polite">
      {label}...
    </div>
  );
}
