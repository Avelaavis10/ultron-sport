type WorkflowHintProps = {
  title?: string;
  steps: string[];
};

export function WorkflowHint({ title = "Recommended flow", steps }: WorkflowHintProps) {
  return (
    <aside className="workflow-hint">
      <strong>{title}</strong>
      <ol>
        {steps.map((step) => (
          <li key={step}>{step}</li>
        ))}
      </ol>
    </aside>
  );
}
