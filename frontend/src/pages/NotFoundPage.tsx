import { Link } from "react-router-dom";

export function NotFoundPage() {
  return (
    <div className="page narrow">
      <h1>Not found</h1>
      <p>The requested prototype route does not exist.</p>
      <Link className="button-link" to="/dashboard">
        Back to dashboard
      </Link>
    </div>
  );
}
