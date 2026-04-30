import { useEffect, useState } from "react";
import { notificationApi } from "../../api/notificationApi";
import { ApiErrorMessage } from "../ApiErrorMessage";
import { EmptyState } from "../EmptyState";
import { LoadingState } from "../LoadingState";
import { StatusPill } from "../StatusPill";
import { SuccessMessage } from "../SuccessMessage";
import type { NotificationResponse } from "../../types/apiTypes";

type NotificationSectionProps = {
  title?: string;
  refreshSignal?: number;
};

export function NotificationSection({ title = "Notifications", refreshSignal = 0 }: NotificationSectionProps) {
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<unknown>(null);
  const [message, setMessage] = useState("");

  async function load() {
    setLoading(true);
    setError(null);
    try {
      const [page, count] = await Promise.all([notificationApi.list(), notificationApi.unreadCount()]);
      setNotifications(page.content);
      setUnreadCount(count.unreadCount);
    } catch (err) {
      setError(err);
    } finally {
      setLoading(false);
    }
  }

  async function markRead(notificationId: number) {
    setError(null);
    try {
      await notificationApi.markRead(notificationId);
      setMessage("Notification marked as read.");
      await load();
    } catch (err) {
      setError(err);
    }
  }

  async function markAllRead() {
    setError(null);
    try {
      await notificationApi.markAllRead();
      setMessage("All notifications marked as read.");
      await load();
    } catch (err) {
      setError(err);
    }
  }

  useEffect(() => {
    void load();
  }, [refreshSignal]);

  return (
    <section className="panel">
      <div className="section-heading">
        <div>
          <h2>{title}</h2>
          <p className="muted">Unread: {unreadCount}</p>
        </div>
        <div className="button-row">
          <button type="button" onClick={() => void load()}>
            Refresh
          </button>
          <button type="button" className="secondary" onClick={() => void markAllRead()}>
            Mark all read
          </button>
        </div>
      </div>

      {loading && <LoadingState />}
      <SuccessMessage message={message} />
      <ApiErrorMessage error={error} />
      {!loading && notifications.length === 0 ? (
        <EmptyState title="No notifications" detail="Workflow notifications will appear here." />
      ) : (
        <div className="card-list compact-list">
          {notifications.slice(0, 6).map((notification) => (
            <article key={notification.id} className="workflow-card">
              <div>
                <div className="row-title">
                  <strong>{notification.title}</strong>
                  <StatusPill value={notification.status} />
                </div>
                <p>{notification.message}</p>
                <small className="muted">
                  {notification.type} - {notification.targetType}
                  {notification.targetId ? ` #${notification.targetId}` : ""}
                </small>
              </div>
              {notification.status === "UNREAD" && (
                <button type="button" className="secondary" onClick={() => void markRead(notification.id)}>
                  Mark read
                </button>
              )}
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
