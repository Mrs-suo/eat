package com.eat.controller;

import com.eat.service.NotificationQueryService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationQueryService notificationQueryService;

    @GetMapping
    public List<Map<String, Object>> getNotifications(@RequestParam String userId) {
        return notificationQueryService.getCurrentMessages(userId);
    }
}
