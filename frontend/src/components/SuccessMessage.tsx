export function SuccessMessage({ message }: { message?: string }) {
  if (!message) {
    return null;
  }

  return (
    <div className="alert success" role="status">
      {message}
    </div>
  );
}
