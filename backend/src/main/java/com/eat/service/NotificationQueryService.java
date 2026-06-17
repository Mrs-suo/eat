package com.eat.service;

import com.eat.entity.AppUser;
import com.eat.entity.DishEditRequest;
import com.eat.entity.Family;
import com.eat.entity.FamilyInvitation;
import com.eat.repository.AppUserRepository;
import com.eat.repository.DishEditRequestRepository;
import com.eat.repository.FamilyInvitationRepository;
import com.eat.repository.FamilyMemberRepository;
import com.eat.repository.FamilyRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationQueryService {

    private static final String INVITE_PENDING = "PENDING";
    private static final String TYPE_JOIN_REQUEST = "JOIN_REQUEST";

    private final DishEditRequestRepository dishEditRequestRepository;
    private final FamilyInvitationRepository familyInvitationRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final AppUserRepository appUserRepository;

    public List<Map<String, Object>> getCurrentMessages(String userId) {
        if (userId == null || userId.isBlank()) {
            return List.of();
        }

        List<Map<String, Object>> messages = new ArrayList<>();
        appendDishRequests(userId, messages);
        appendJoinRequests(userId, messages);

        messages.sort(Comparator.comparing(
                item -> (LocalDateTime) item.getOrDefault("createTime", LocalDateTime.MIN),
                Comparator.reverseOrder()
        ));
        return messages;
    }

    private void appendDishRequests(String userId, List<Map<String, Object>> messages) {
        appUserRepository.findByUserId(userId).ifPresent(user -> {
            Long familyId = user.getFamilyId();
            if (familyId == null || familyMemberRepository.findByFamilyIdAndUserId(familyId, userId).isEmpty()) {
                return;
            }
            Family family = familyRepository.findById(familyId).orElse(null);
            List<DishEditRequest> requests = dishEditRequestRepository.findByFamilyIdAndStatus(familyId, 0);
            for (DishEditRequest request : requests) {
                Map<String, Object> item = base("dish", request.getId(), "菜品审核", request.getCreateTime());
                item.put("title", request.getOriginalDish() == null ? "新增菜品申请" : "菜品修改申请");
                item.put("content", request.getName());
                item.put("familyId", familyId);
                item.put("familyName", family == null ? "当前家庭" : family.getName());
                item.put("applicantUserId", request.getUserId());
                item.put("applicantName", userName(request.getUserId()));
                messages.add(item);
            }
        });
    }

    private void appendJoinRequests(String userId, List<Map<String, Object>> messages) {
        List<Long> familyIds = familyMemberRepository.findByUserId(userId)
                .stream()
                .map(member -> member.getFamilyId())
                .toList();
        for (Long familyId : familyIds) {
            Family family = familyRepository.findById(familyId).orElse(null);
            if (!isCreator(family, userId)) {
                continue;
            }
            List<FamilyInvitation> invitations = familyInvitationRepository.findByFamilyIdOrderByCreateTimeDesc(familyId);
            for (FamilyInvitation invitation : invitations) {
                if (!INVITE_PENDING.equals(invitation.getStatus()) || !TYPE_JOIN_REQUEST.equals(invitation.getRequestType())) {
                    continue;
                }
                Map<String, Object> item = base("familyJoin", invitation.getId(), "家庭加入申请", invitation.getCreateTime());
                item.put("content", userName(invitation.getInviteeUserId()) + " 申请加入 " + (family == null ? "家庭" : family.getName()));
                item.put("familyId", familyId);
                item.put("familyName", family == null ? "家庭" : family.getName());
                item.put("familyCode", family == null ? "" : family.getFamilyCode());
                item.put("applicantUserId", invitation.getInviteeUserId());
                item.put("applicantPhone", invitation.getInviteePhone());
                item.put("applicantName", userName(invitation.getInviteeUserId()));
                messages.add(item);
            }
        }
    }

    private Map<String, Object> base(String auditType, Long auditId, String title, LocalDateTime createTime) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", auditType + "-" + auditId);
        item.put("auditType", auditType);
        item.put("auditId", auditId);
        item.put("title", title);
        item.put("createTime", createTime);
        return item;
    }

    private boolean isCreator(Family family, String userId) {
        if (family == null) {
            return false;
        }
        String creatorUserId = family.getCreatorUserId();
        if (creatorUserId == null || creatorUserId.isBlank()) {
            return familyMemberRepository.findByFamilyId(family.getId())
                    .stream()
                    .findFirst()
                    .map(member -> userId.equals(member.getUserId()))
                    .orElse(false);
        }
        return creatorUserId.equals(userId);
    }

    private String userName(String userId) {
        return appUserRepository.findByUserId(userId)
                .map(this::displayName)
                .orElse("家人");
    }

    private String displayName(AppUser user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        return maskPhone(user.getPhone());
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone == null ? "" : phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
