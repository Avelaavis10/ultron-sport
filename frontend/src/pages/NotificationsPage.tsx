import { useEffect, useState } from "react";
import { notificationApi } from "../api/notificationApi";
import { ApiErrorMessage } from "../components/ApiErrorMessage";
import { EmptyState } from "../components/EmptyState";
import { LoadingState } from "../components/LoadingState";
import { PageHeader } from "../components/PageHeader";
import { StatusPill } from "../components/StatusPill";
import { SuccessMessage } from "../components/SuccessMessage";
import type { NotificationResponse } from "../types/apiTypes";

export function NotificationsPage() {
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

  async function markRead(id: number) {
    setError(null);
    try {
      await notificationApi.markRead(id);
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
  }, []);

  return (
    <div className="page">
      <PageHeader title="Notifications" description={`Unread notifications: ${unreadCount}`}>
        <button type="button" onClick={load}>
          Refresh
        </button>
        <button type="button" onClick={markAllRead}>
          Mark all read
        </button>
      </PageHeader>
      {loading && <LoadingState />}
      <SuccessMessage message={message} />
      <ApiErrorMessage error={error} />
      {notifications.length === 0 && !loading ? (
        <EmptyState title="No notifications" detail="Workflow notifications will appear here." />
      ) : (
        <div className="list">
          {notifications.map((notification) => (
            <article key={notification.id} className="row">
              <div>
                <div className="row-title">
                  <strong>{notification.title}</strong>
                  <StatusPill value={notification.status} />
                </div>
                <p>{notification.message}</p>
                <small>
                  {notification.type} - {notification.targetType}
                  {notification.targetId ? ` #${notification.targetId}` : ""}
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
