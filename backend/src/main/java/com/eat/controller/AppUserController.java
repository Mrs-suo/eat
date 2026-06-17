package com.eat.controller;

import com.eat.entity.AppUser;
import com.eat.entity.Family;
import com.eat.service.AppUserService;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppUserController {

    private final AppUserService appUserService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        try {
            AppUser user = appUserService.login(payload.get("phone"));
            Family family = appUserService.getCurrentFamily(user.getUserId());
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("user", user);
            body.put("family", family);
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        try {
            AppUserService.RegisterResult result = appUserService.register(
                payload.get("phone"),
                payload.get("nickname"),
                payload.get("familyCode"),
                payload.get("familyName")
            );
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("user", result.user());
            body.put("family", result.family());
            return ResponseEntity.ok(body);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getById(@PathVariable String userId) {
        AppUser user = appUserService.getByUserId(userId);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable String userId, @RequestBody Map<String, String> payload) {
        try {
            AppUser user = appUserService.updateProfile(
                userId,
                payload.get("nickname"),
                payload.get("avatar")
            );
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{userId}/family")
    public ResponseEntity<?> getFamily(@PathVariable String userId) {
        Family family = appUserService.getCurrentFamily(userId);
        if (family == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(family);
    }
}
