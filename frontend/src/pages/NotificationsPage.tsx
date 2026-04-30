import { useEffect, useState } from "react";
import { notificationApi } from "../api/notificationApi";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { EmptyState } from "../components/EmptyState";
import { LoadingState } from "../components/LoadingState";
import type { NotificationResponse } from "../types/apiTypes";

export function NotificationsPage() {
  const [notifications, setNotifications] = useState<NotificationResponse[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<unknown>(null);

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

  async function markRead(id: number) {
    setError(null);
    try {
      await notificationApi.markRead(id);
      await load();
    } catch (err) {
      setError(err);
    }
  }

  async function markAllRead() {
    setError(null);
    try {
      await notificationApi.markAllRead();
      await load();
    } catch (err) {
      setError(err);
    }
  }

  useEffect(() => {
    void load();
  }, []);

  return (
    <div className="page">
      <h1>Notifications</h1>
      <div className="actions">
        <button type="button" onClick={load}>
          Refresh
        </button>
        <button type="button" onClick={markAllRead}>
          Mark all read
        </button>
      </div>
      <p className="muted">Unread: {unreadCount}</p>
      {loading && <LoadingState />}
      <ApiErrorMessage error={error} />
      {notifications.length === 0 && !loading ? (
        <EmptyState title="No notifications" detail="Workflow notifications will appear here." />
      ) : (
        <div className="list">
          {notifications.map((notification) => (
            <article key={notification.id} className="row">
              <div>
                <strong>{notification.title}</strong>
                <p>{notification.message}</p>
                <small>
                  {notification.type} - {notification.status}
                </small>
              </div>
              {notification.status === "UNREAD" && (
                <button type="button" onClick={() => void markRead(notification.id)}>
                  Mark read
                </button>
              )}
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
