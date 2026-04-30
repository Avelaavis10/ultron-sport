# Notifications And Events

## Purpose

Ultron Sport now has a simple in-app notification foundation so users can see important platform activity without adding external delivery infrastructure too early.

## Current Scope

- Notifications are stored in the relational database.
- Users can view only their own notifications.
- Users can list unread notifications and count unread notifications.
- Users can mark one owned notification as read.
- Users can mark all owned notifications as read.
- Notifications are append-only through the service/API surface. Delete and edit endpoints are intentionally not exposed.

## Endpoints

```text
GET  /api/notifications
GET  /api/notifications/unread
GET  /api/notifications/unread-count
POST /api/notifications/{notificationId}/read
POST /api/notifications/read-all
```

List endpoints support `status`, `page`, `size`, `sortBy`, and `sortDirection`. The default page size is `20`, maximum page size is `50`, and default sort is `createdAt DESC`.

## Notification Events

- ATHLETE submits evidence: admins receive an in-app notification as an MVP fallback.
- COACH verifies evidence: athlete receives an evidence verified notification.
- COACH rejects evidence: athlete receives an evidence rejected notification with the reason.
- ADMIN flags evidence: athlete receives an evidence flagged notification.
- ADMIN archives evidence: athlete receives an evidence archived notification.
- LevelPlay score or tier changes: athlete receives a LevelPlay score changed notification.
- ATHLETE creates an achievement: athlete receives an achievement added notification.
- ATHLETE updates profile: athlete receives a profile updated notification.
- ATHLETE links organisation: athlete receives an organisation link notification.
- COACH creates or updates profile: coach receives a coach profile saved notification.

## MVP Decisions

Coach targeting for submitted evidence is not roster-aware yet. The MVP therefore notifies admins and leaves coach-targeted routing for the future coach-athlete roster relationship slice.

Admin recalculate-all does not send score-change notifications to every athlete. This avoids noisy notification fan-out until the platform has preferences and better delivery controls.

Moderation notes are internal and do not notify athletes unless the evidence status changes through flag/archive.

## Deferred Work

The MVP does not implement email, SMS, push notifications, Firebase Cloud Messaging, WebSockets, Kafka, RabbitMQ, Redis, external event streaming, notification preferences, scheduled cleanup, or AI-triggered notification rules.
