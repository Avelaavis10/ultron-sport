import type { ReactNode } from "react";

type FormFieldProps = {
  label: string;
  hint?: string;
  required?: boolean;
  children: ReactNode;
};

export function FormField({ label, hint, required = false, children }: FormFieldProps) {
  return (
    <label className="field">
      <span className="field-label">
        {label}
        {required && <span className="required-mark">Required</span>}
      </span>
      {children}
      {hint && <small className="field-hint">{hint}</small>}
    </label>
  );
}
