package com.eat.controller;

import com.eat.entity.DishEditRequest;
import com.eat.service.DishEditRequestService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/edit-requests")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DishEditRequestController {

    private final DishEditRequestService requestService;

    @GetMapping("/pending")
    public List<DishEditRequest> getPending(@RequestParam(required = false) Long familyId) {
        return requestService.getPendingByFamily(familyId);
    }

    @GetMapping("/family/{familyId}")
    public List<DishEditRequest> getByFamily(@PathVariable Long familyId) {
        return requestService.getAllByFamily(familyId);
    }

    @GetMapping("/user/{userId}")
    public List<DishEditRequest> getRequestsByUserId(@PathVariable String userId) {
        return requestService.getRequestsByUserId(userId);
    }

    @PostMapping
    public ResponseEntity<?> createRequest(@RequestBody DishEditRequest request) {
        try {
            return ResponseEntity.ok(requestService.createRequest(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<DishEditRequest> approveRequest(@PathVariable Long id) {
        DishEditRequest result = requestService.approveRequest(id);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<DishEditRequest> rejectRequest(@PathVariable Long id, @RequestParam String reason) {
        DishEditRequest result = requestService.rejectRequest(id, reason);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@PathVariable Long id) {
        requestService.deleteRequest(id);
        return ResponseEntity.ok().build();
    }
}
