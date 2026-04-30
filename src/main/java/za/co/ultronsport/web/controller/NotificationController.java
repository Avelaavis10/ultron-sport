package za.co.ultronsport.web.controller;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import za.co.ultronsport.config.security.SecurityUser;
import za.co.ultronsport.domain.Notification;
import za.co.ultronsport.service.NotificationService;
import za.co.ultronsport.web.dto.MarkAllNotificationsReadResponse;
import za.co.ultronsport.web.dto.MarkNotificationReadResponse;
import za.co.ultronsport.web.dto.NotificationResponse;
import za.co.ultronsport.web.dto.NotificationSearchCriteria;
import za.co.ultronsport.web.dto.NotificationUnreadCountResponse;
import za.co.ultronsport.web.dto.PageResponse;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public PageResponse<NotificationResponse> list(@RequestParam(required = false) String status,
                                                   @RequestParam(defaultValue = "0") Integer page,
                                                   @RequestParam(defaultValue = "20") Integer size,
                                                   @RequestParam(defaultValue = "createdAt") String sortBy,
                                                   @RequestParam(defaultValue = "DESC") String sortDirection,
                                                   Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        NotificationSearchCriteria criteria = NotificationSearchCriteria.from(status, page, size, sortBy,
                sortDirection);
        Page<Notification> notifications = notificationService.getMyNotifications(currentUser.getId(), criteria);
        return response(notifications, criteria);
    }

    @GetMapping("/unread")
    public PageResponse<NotificationResponse> unread(@RequestParam(defaultValue = "0") Integer page,
                                                     @RequestParam(defaultValue = "20") Integer size,
                                                     @RequestParam(defaultValue = "createdAt") String sortBy,
                                                     @RequestParam(defaultValue = "DESC") String sortDirection,
                                                     Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        NotificationSearchCriteria criteria = NotificationSearchCriteria.from(null, page, size, sortBy,
                sortDirection).unreadOnly();
        Page<Notification> notifications = notificationService.getMyUnreadNotifications(currentUser.getId(),
                criteria);
        return response(notifications, criteria);
    }

    @GetMapping("/unread-count")
    public NotificationUnreadCountResponse unreadCount(Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return new NotificationUnreadCountResponse(notificationService.countMyUnreadNotifications(currentUser.getId()));
    }

    @PostMapping("/{notificationId}/read")
    public MarkNotificationReadResponse markRead(@PathVariable Long notificationId, Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return MarkNotificationReadResponse.from(notificationService.markAsRead(currentUser.getId(),
                notificationId));
    }

    @PostMapping("/read-all")
    public MarkAllNotificationsReadResponse markAllRead(Authentication authentication) {
        SecurityUser currentUser = currentUser(authentication);
        return new MarkAllNotificationsReadResponse(notificationService.markAllMineAsRead(currentUser.getId()));
    }

    private PageResponse<NotificationResponse> response(Page<Notification> notifications,
                                                        NotificationSearchCriteria criteria) {
        List<NotificationResponse> content = notifications.getContent().stream()
                .map(NotificationResponse::from)
                .toList();
        return PageResponse.from(notifications, content, criteria.effectiveSortBy(), criteria.direction());
    }

    private SecurityUser currentUser(Authentication authentication) {
        return (SecurityUser) authentication.getPrincipal();
    }
}
