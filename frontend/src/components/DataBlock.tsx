export function DataBlock({ title, data }: { title: string; data: unknown }) {
  if (data === null || data === undefined) {
    return null;
  }

  return (
    <section className="data-block">
      <h4>{title}</h4>
      <pre>{JSON.stringify(data, null, 2)}</pre>
    </section>
  );
}
