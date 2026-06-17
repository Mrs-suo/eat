package com.eat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<String, Set<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();

    public NotificationWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = readUserId(session.getUri());
        if (userId == null || userId.isBlank()) {
            return;
        }
        session.getAttributes().put("userId", userId);
        sessionsByUser.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        removeSession(session);
    }

    public void sendToUser(String userId, Object payload) {
        Set<WebSocketSession> sessions = sessionsByUser.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        try {
            TextMessage message = new TextMessage(objectMapper.writeValueAsString(payload));
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            }
        } catch (Exception ignored) {
            // Notification refreshes are best-effort; REST remains the source of truth.
        }
    }

    private void removeSession(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        if (userId == null) {
            return;
        }
        Set<WebSocketSession> sessions = sessionsByUser.get(String.valueOf(userId));
        if (sessions != null) {
            sessions.remove(session);
        }
    }

    private String readUserId(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            return null;
        }
        String[] pairs = uri.getQuery().split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && "userId".equals(parts[0])) {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
