package com.eat.controller;

import com.eat.entity.AppUser;
import com.eat.entity.DailyCook;
import com.eat.entity.Family;
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

    @GetMapping("/code/{code}")
    public ResponseEntity<?> previewByCode(@PathVariable String code) {
        Family family = familyService.getFamilyByCode(code);
        if (family == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "家庭码无效"));
        }
        long count = familyService.getFamilyMembers(family.getId()).size();
        return ResponseEntity.ok(Map.of(
            "family", family,
            "memberCount", count
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

    @GetMapping("/{familyId}/members")
    public List<AppUser> getMembers(@PathVariable Long familyId) {
        return familyService.getFamilyMembers(familyId);
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
