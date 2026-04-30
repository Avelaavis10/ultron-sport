import { apiRequest, type PageResponse } from "./apiClient";
import type { NotificationResponse, NotificationUnreadCountResponse } from "../types/apiTypes";

export const notificationApi = {
  list: (status?: string) =>
    apiRequest<PageResponse<NotificationResponse>>("/api/notifications", { query: { status } }),
  unread: () => apiRequest<PageResponse<NotificationResponse>>("/api/notifications/unread"),
  unreadCount: () => apiRequest<NotificationUnreadCountResponse>("/api/notifications/unread-count"),
  markRead: (notificationId: number) =>
    apiRequest<{ notificationId: number; status: string; readAt: string }>(`/api/notifications/${notificationId}/read`, {
      method: "POST"
    }),
  markAllRead: () =>
    apiRequest<{ markedReadCount: number }>("/api/notifications/read-all", { method: "POST" })
};
