package com.eat.service;

import com.eat.entity.FamilyMember;
import com.eat.repository.FamilyMemberRepository;
import com.eat.websocket.NotificationWebSocketHandler;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationPushService {

    private final NotificationWebSocketHandler webSocketHandler;
    private final FamilyMemberRepository familyMemberRepository;

    public void refreshUser(String userId) {
        if (userId == null || userId.isBlank()) {
            return;
        }
        webSocketHandler.sendToUser(userId, Map.of("type", "REFRESH_NOTIFICATIONS"));
    }

    public void refreshFamily(Long familyId) {
        if (familyId == null) {
            return;
        }
        familyMemberRepository.findByFamilyId(familyId)
                .stream()
                .map(FamilyMember::getUserId)
                .distinct()
                .forEach(this::refreshUser);
    }
}
