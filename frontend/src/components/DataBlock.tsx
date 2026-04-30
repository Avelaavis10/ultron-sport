export function DataBlock({ title, data, defaultOpen = false }: { title: string; data: unknown; defaultOpen?: boolean }) {
  if (data === null || data === undefined) {
    return null;
  }

  return (
    <details className="data-block" open={defaultOpen}>
      <summary>{title}</summary>
      <pre>{JSON.stringify(data, null, 2)}</pre>
    </details>
  );
}
