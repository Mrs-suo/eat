package com.eat.controller;

import com.eat.entity.AppUser;
import com.eat.entity.DailyCook;
import com.eat.entity.Family;
import com.eat.entity.FamilyInvitation;
import com.eat.service.FamilyService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/families")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FamilyController {

    private final FamilyService familyService;

    @PostMapping
    public ResponseEntity<?> createFamily(@RequestBody Map<String, String> payload) {
        try {
            AppUser creator = familyService.getUser(payload.get("userId"));
            return ResponseEntity.ok(familyService.createFamily(payload.get("name"), creator));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<?> previewByCode(@PathVariable String code) {
        Family family = familyService.getFamilyByCode(code);
        if (family == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "家庭码无效"));
        }
        long count = familyService.getFamilyMembers(family.getId()).size();
        return ResponseEntity.ok(Map.of(
            "family", family,
            "memberCount", count,
            "maxMembers", FamilyService.MAX_FAMILY_MEMBERS
        ));
    }

    @GetMapping("/{familyId}")
    public ResponseEntity<?> getFamily(@PathVariable Long familyId) {
        Family family = familyService.getFamily(familyId);
        if (family == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(family);
    }

    @GetMapping("/by-user/{userId}")
    public List<Family> getUserFamilies(@PathVariable String userId) {
        return familyService.getUserFamilies(userId);
    }

    @PostMapping("/{familyId}/switch-current")
    public ResponseEntity<?> switchCurrentFamily(
        @PathVariable Long familyId,
        @RequestBody Map<String, String> payload
    ) {
        try {
            return ResponseEntity.ok(familyService.switchCurrentFamily(payload.get("userId"), familyId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{familyId}")
    public ResponseEntity<?> updateFamily(
        @PathVariable Long familyId,
        @RequestBody Map<String, String> payload
    ) {
        try {
            return ResponseEntity.ok(familyService.updateFamily(
                familyId,
                payload.get("userId"),
                payload.get("name")
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{familyId}/members")
    public List<AppUser> getMembers(@PathVariable Long familyId) {
        return familyService.getFamilyMembers(familyId);
    }

    @PostMapping("/{familyId}/invitations")
    public ResponseEntity<?> inviteMember(
        @PathVariable Long familyId,
        @RequestBody Map<String, String> payload
    ) {
        try {
            FamilyInvitation invitation = familyService.inviteMember(
                familyId,
                payload.get("userId"),
                payload.get("phone")
            );
            return ResponseEntity.ok(invitation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{familyId}/invitations")
    public ResponseEntity<?> getFamilyInvitations(
        @PathVariable Long familyId,
        @RequestParam String userId
    ) {
        try {
            return ResponseEntity.ok(familyService.getFamilyInvitations(familyId, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/invitations/received")
    public List<Map<String, Object>> getReceivedInvitations(@RequestParam String userId) {
        return familyService.getReceivedInvitations(userId);
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<?> acceptInvitation(
        @PathVariable Long invitationId,
        @RequestBody Map<String, String> payload
    ) {
        try {
            return ResponseEntity.ok(familyService.acceptInvitation(invitationId, payload.get("userId")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/invitations/{invitationId}/reject")
    public ResponseEntity<?> rejectInvitation(
        @PathVariable Long invitationId,
        @RequestBody Map<String, String> payload
    ) {
        try {
            familyService.rejectInvitation(invitationId, payload.get("userId"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/join-requests/{invitationId}/approve")
    public ResponseEntity<?> approveJoinRequest(
        @PathVariable Long invitationId,
        @RequestBody Map<String, String> payload
    ) {
        try {
            return ResponseEntity.ok(familyService.approveJoinRequest(invitationId, payload.get("userId")));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/join-requests")
    public ResponseEntity<?> requestJoinFamily(@RequestBody Map<String, String> payload) {
        try {
            return ResponseEntity.ok(familyService.requestJoinByTarget(
                payload.get("target"),
                payload.get("userId")
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/join-requests/{invitationId}/reject")
    public ResponseEntity<?> rejectJoinRequest(
        @PathVariable Long invitationId,
        @RequestBody Map<String, String> payload
    ) {
        try {
            familyService.rejectJoinRequest(invitationId, payload.get("userId"));
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{familyId}/members/{targetUserId}")
    public ResponseEntity<?> removeMember(
        @PathVariable Long familyId,
        @PathVariable String targetUserId,
        @RequestParam String userId
    ) {
        try {
            familyService.removeMember(familyId, userId, targetUserId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{familyId}/today-cook")
    public ResponseEntity<?> getTodayCook(
        @PathVariable Long familyId,
        @RequestParam(required = false) String date
    ) {
        try {
            LocalDate d = (date == null || date.isEmpty()) ? LocalDate.now() : LocalDate.parse(date);
            DailyCook cook = familyService.getOrCreateTodayCook(familyId, d);
            AppUser cookUser = cook == null ? null : null;
            return ResponseEntity.ok(cook);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{familyId}/switch-cook")
    public ResponseEntity<?> switchCook(
        @PathVariable Long familyId,
        @RequestBody Map<String, String> payload
    ) {
        try {
            String userId = payload.get("userId");
            String dateStr = payload.get("date");
            if (userId == null || userId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "缺少 userId"));
            }
            LocalDate d = (dateStr == null || dateStr.isEmpty()) ? LocalDate.now() : LocalDate.parse(dateStr);
            return ResponseEntity.ok(familyService.switchCook(familyId, userId, d));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{familyId}/cook-history")
    public List<DailyCook> getCookHistory(@PathVariable Long familyId) {
        return familyService.getRecentCookHistory(familyId);
    }
}
