package com.eat.service;

import com.eat.entity.AppUser;
import com.eat.entity.DailyCook;
import com.eat.entity.Family;
import com.eat.entity.FamilyInvitation;
import com.eat.entity.FamilyMember;
import com.eat.repository.AppUserRepository;
import com.eat.repository.DailyCookRepository;
import com.eat.repository.FamilyInvitationRepository;
import com.eat.repository.FamilyMemberRepository;
import com.eat.repository.FamilyRepository;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    public static final int MAX_FAMILY_MEMBERS = 6;
    private static final String INVITE_PENDING = "PENDING";
    private static final String INVITE_ACCEPTED = "ACCEPTED";
    private static final String INVITE_REJECTED = "REJECTED";
    private static final String TYPE_INVITE = "INVITE";
    private static final String TYPE_JOIN_REQUEST = "JOIN_REQUEST";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final AppUserRepository appUserRepository;
    private final DailyCookRepository dailyCookRepository;
    private final FamilyInvitationRepository familyInvitationRepository;

    public AppUser getUser(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("用户不存在");
        }
        return appUserRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    @Transactional
    public Family createFamily(String familyName, AppUser creator) {
        String cleanName = familyName == null ? "" : familyName.trim();
        if (cleanName.isEmpty()) {
            throw new IllegalArgumentException("请输入家庭名称");
        }
        Family family = new Family();
        family.setName(cleanName);
        family.setCreatorUserId(creator.getUserId());
        family.setFamilyCode(generateUniqueCode());
        family.setCreateTime(LocalDateTime.now());
        family = familyRepository.save(family);

        addMember(family.getId(), creator.getUserId());
        setCurrentFamilyIfNeeded(creator, family.getId());
        return family;
    }

    @Transactional
    public Family joinFamily(String familyCode, AppUser user) {
        Family family = familyRepository.findByFamilyCode(familyCode.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("家庭码无效"));
        if (familyMemberRepository.findByFamilyIdAndUserId(family.getId(), user.getUserId()).isPresent()) {
            return family;
        }
        long currentCount = familyMemberRepository.countByFamilyId(family.getId());
        if (currentCount >= MAX_FAMILY_MEMBERS) {
            throw new IllegalArgumentException("家庭成员已满（最多 " + MAX_FAMILY_MEMBERS + " 人）");
        }
        addMember(family.getId(), user.getUserId());
        setCurrentFamilyIfNeeded(user, family.getId());
        return family;
    }

    private void addMember(Long familyId, String userId) {
        if (familyMemberRepository.findByFamilyIdAndUserId(familyId, userId).isPresent()) {
            return;
        }
        FamilyMember member = new FamilyMember();
        member.setFamilyId(familyId);
        member.setUserId(userId);
        member.setJoinedAt(LocalDateTime.now());
        familyMemberRepository.save(member);
    }

    private void setCurrentFamilyIfNeeded(AppUser user, Long familyId) {
        if (user.getFamilyId() == null) {
            user.setFamilyId(familyId);
            user.setUpdateTime(LocalDateTime.now());
            appUserRepository.save(user);
        }
    }

    public Family getFamily(Long familyId) {
        return familyRepository.findById(familyId).orElse(null);
    }

    public Family getFamilyByCode(String code) {
        return familyRepository.findByFamilyCode(code.toUpperCase()).orElse(null);
    }

    public List<AppUser> getFamilyMembers(Long familyId) {
        List<FamilyMember> members = familyMemberRepository.findByFamilyId(familyId);
        List<AppUser> users = new ArrayList<>();
        for (FamilyMember m : members) {
            appUserRepository.findByUserId(m.getUserId()).ifPresent(users::add);
        }
        return users;
    }

    public List<Family> getUserFamilies(String userId) {
        List<Long> ids = familyMemberRepository.findByUserId(userId)
                .stream()
                .map(FamilyMember::getFamilyId)
                .toList();
        if (ids.isEmpty()) return List.of();
        return familyRepository.findByIdIn(ids);
    }

    @Transactional
    public Family switchCurrentFamily(String userId, Long familyId) {
        AppUser user = appUserRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        requireMember(familyId, userId);
        user.setFamilyId(familyId);
        user.setUpdateTime(LocalDateTime.now());
        appUserRepository.save(user);
        return getFamily(familyId);
    }

    @Transactional
    public Family updateFamily(Long familyId, String operatorUserId, String name) {
        requireMember(familyId, operatorUserId);
        String cleanName = name == null ? "" : name.trim();
        if (cleanName.isEmpty()) {
            throw new IllegalArgumentException("请输入家庭名称");
        }
        if (cleanName.length() > 64) {
            throw new IllegalArgumentException("家庭名称最多 64 个字符");
        }
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("家庭不存在"));
        family.setName(cleanName);
        return familyRepository.save(family);
    }

    @Transactional
    public FamilyInvitation inviteMember(Long familyId, String inviterUserId, String phone) {
        requireMember(familyId, inviterUserId);
        String cleanPhone = normalizePhone(phone);
        AppUser invitee = appUserRepository.findByPhone(cleanPhone)
                .orElseThrow(() -> new IllegalArgumentException("该手机号还没有注册"));
        if (inviterUserId.equals(invitee.getUserId())) {
            throw new IllegalArgumentException("不能邀请自己");
        }
        if (familyMemberRepository.findByFamilyIdAndUserId(familyId, invitee.getUserId()).isPresent()) {
            throw new IllegalArgumentException("对方已经在这个家庭里");
        }
        if (familyMemberRepository.countByFamilyId(familyId) >= MAX_FAMILY_MEMBERS) {
            throw new IllegalArgumentException("家庭成员已满（最多 " + MAX_FAMILY_MEMBERS + " 人）");
        }
        familyInvitationRepository.findByFamilyIdAndInviteeUserIdAndStatus(familyId, invitee.getUserId(), INVITE_PENDING)
                .ifPresent(invitation -> {
                    throw new IllegalArgumentException("已经邀请过对方，等待对方确认");
                });

        FamilyInvitation invitation = new FamilyInvitation();
        invitation.setFamilyId(familyId);
        invitation.setInviterUserId(inviterUserId);
        invitation.setInviteeUserId(invitee.getUserId());
        invitation.setInviteePhone(invitee.getPhone());
        invitation.setStatus(INVITE_PENDING);
        invitation.setRequestType(TYPE_INVITE);
        invitation.setCreateTime(LocalDateTime.now());
        invitation.setUpdateTime(LocalDateTime.now());
        return familyInvitationRepository.save(invitation);
    }

    @Transactional
    public FamilyInvitation requestJoinFamily(String familyCode, AppUser applicant) {
        Family family = familyRepository.findByFamilyCode(familyCode.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("家庭码无效"));
        return createJoinRequest(family, applicant);
    }

    @Transactional
    public FamilyInvitation requestJoinByTarget(String target, String applicantUserId) {
        AppUser applicant = getUser(applicantUserId);
        String cleanTarget = target == null ? "" : target.trim();
        if (cleanTarget.isEmpty()) {
            throw new IllegalArgumentException("请输入手机号或家庭码");
        }

        Family family;
        if (cleanTarget.matches("^1\\d{10}$")) {
            AppUser owner = appUserRepository.findByPhone(cleanTarget)
                    .orElseThrow(() -> new IllegalArgumentException("该手机号还没有注册"));
            if (owner.getFamilyId() == null) {
                throw new IllegalArgumentException("对方还没有绑定家庭");
            }
            family = familyRepository.findById(owner.getFamilyId())
                    .orElseThrow(() -> new IllegalArgumentException("对方当前家庭不存在"));
        } else {
            if (!cleanTarget.matches("^[A-Za-z0-9]{6}$")) {
                throw new IllegalArgumentException("请输入正确的手机号或 6 位家庭码");
            }
            family = familyRepository.findByFamilyCode(cleanTarget.toUpperCase())
                    .orElseThrow(() -> new IllegalArgumentException("家庭码无效"));
        }
        return createJoinRequest(family, applicant);
    }

    private FamilyInvitation createJoinRequest(Family family, AppUser applicant) {
        if (applicant.getUserId().equals(family.getCreatorUserId())) {
            throw new IllegalArgumentException("你已经是这个家庭的创建人");
        }
        if (familyMemberRepository.findByFamilyIdAndUserId(family.getId(), applicant.getUserId()).isPresent()) {
            throw new IllegalArgumentException("你已经在这个家庭里");
        }
        if (familyMemberRepository.countByFamilyId(family.getId()) >= MAX_FAMILY_MEMBERS) {
            throw new IllegalArgumentException("家庭成员已满（最多 " + MAX_FAMILY_MEMBERS + " 人）");
        }
        familyInvitationRepository.findByFamilyIdAndInviteeUserIdAndStatus(family.getId(), applicant.getUserId(), INVITE_PENDING)
                .ifPresent(invitation -> {
                    throw new IllegalArgumentException("已提交申请，等待家庭成员审核");
                });

        FamilyInvitation invitation = new FamilyInvitation();
        invitation.setFamilyId(family.getId());
        invitation.setInviterUserId(applicant.getUserId());
        invitation.setInviteeUserId(applicant.getUserId());
        invitation.setInviteePhone(applicant.getPhone());
        invitation.setStatus(INVITE_PENDING);
        invitation.setRequestType(TYPE_JOIN_REQUEST);
        invitation.setCreateTime(LocalDateTime.now());
        invitation.setUpdateTime(LocalDateTime.now());
        return familyInvitationRepository.save(invitation);
    }

    public List<Map<String, Object>> getFamilyInvitations(Long familyId, String operatorUserId) {
        requireCreator(familyId, operatorUserId);
        return familyInvitationRepository.findByFamilyIdOrderByCreateTimeDesc(familyId)
                .stream()
                .map(this::toInvitationView)
                .toList();
    }

    public List<Map<String, Object>> getReceivedInvitations(String userId) {
        return familyInvitationRepository.findByInviteeUserIdAndStatusOrderByCreateTimeDesc(userId, INVITE_PENDING)
                .stream()
                .filter(invitation -> TYPE_INVITE.equals(invitation.getRequestType()))
                .map(this::toInvitationView)
                .toList();
    }

    @Transactional
    public Family acceptInvitation(Long invitationId, String userId) {
        FamilyInvitation invitation = familyInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("邀请不存在"));
        if (!userId.equals(invitation.getInviteeUserId())) {
            throw new IllegalArgumentException("这不是你的邀请");
        }
        if (!INVITE_PENDING.equals(invitation.getStatus())) {
            throw new IllegalArgumentException("邀请已处理");
        }
        if (!TYPE_INVITE.equals(invitation.getRequestType())) {
            throw new IllegalArgumentException("这不是家庭邀请");
        }
        if (familyMemberRepository.findByFamilyIdAndUserId(invitation.getFamilyId(), userId).isEmpty()
                && familyMemberRepository.countByFamilyId(invitation.getFamilyId()) >= MAX_FAMILY_MEMBERS) {
            throw new IllegalArgumentException("家庭成员已满（最多 " + MAX_FAMILY_MEMBERS + " 人）");
        }
        addMember(invitation.getFamilyId(), userId);
        AppUser user = appUserRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setFamilyId(invitation.getFamilyId());
        user.setUpdateTime(LocalDateTime.now());
        appUserRepository.save(user);

        invitation.setStatus(INVITE_ACCEPTED);
        invitation.setUpdateTime(LocalDateTime.now());
        familyInvitationRepository.save(invitation);
        return getFamily(invitation.getFamilyId());
    }

    @Transactional
    public void rejectInvitation(Long invitationId, String userId) {
        FamilyInvitation invitation = familyInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("邀请不存在"));
        if (!userId.equals(invitation.getInviteeUserId())) {
            throw new IllegalArgumentException("这不是你的邀请");
        }
        if (!INVITE_PENDING.equals(invitation.getStatus())) {
            throw new IllegalArgumentException("邀请已处理");
        }
        if (!TYPE_INVITE.equals(invitation.getRequestType())) {
            throw new IllegalArgumentException("这不是家庭邀请");
        }
        invitation.setStatus(INVITE_REJECTED);
        invitation.setUpdateTime(LocalDateTime.now());
        familyInvitationRepository.save(invitation);
    }

    @Transactional
    public Family approveJoinRequest(Long invitationId, String operatorUserId) {
        FamilyInvitation invitation = familyInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在"));
        requireCreator(invitation.getFamilyId(), operatorUserId);
        if (!TYPE_JOIN_REQUEST.equals(invitation.getRequestType())) {
            throw new IllegalArgumentException("这不是入家申请");
        }
        if (!INVITE_PENDING.equals(invitation.getStatus())) {
            throw new IllegalArgumentException("申请已处理");
        }
        if (familyMemberRepository.findByFamilyIdAndUserId(invitation.getFamilyId(), invitation.getInviteeUserId()).isEmpty()
                && familyMemberRepository.countByFamilyId(invitation.getFamilyId()) >= MAX_FAMILY_MEMBERS) {
            throw new IllegalArgumentException("家庭成员已满（最多 " + MAX_FAMILY_MEMBERS + " 人）");
        }
        addMember(invitation.getFamilyId(), invitation.getInviteeUserId());
        AppUser applicant = getUser(invitation.getInviteeUserId());
        if (applicant.getFamilyId() == null) {
            applicant.setFamilyId(invitation.getFamilyId());
            applicant.setUpdateTime(LocalDateTime.now());
            appUserRepository.save(applicant);
        }
        invitation.setStatus(INVITE_ACCEPTED);
        invitation.setUpdateTime(LocalDateTime.now());
        familyInvitationRepository.save(invitation);
        return getFamily(invitation.getFamilyId());
    }

    @Transactional
    public void rejectJoinRequest(Long invitationId, String operatorUserId) {
        FamilyInvitation invitation = familyInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("申请不存在"));
        requireCreator(invitation.getFamilyId(), operatorUserId);
        if (!TYPE_JOIN_REQUEST.equals(invitation.getRequestType())) {
            throw new IllegalArgumentException("这不是入家申请");
        }
        if (!INVITE_PENDING.equals(invitation.getStatus())) {
            throw new IllegalArgumentException("申请已处理");
        }
        invitation.setStatus(INVITE_REJECTED);
        invitation.setUpdateTime(LocalDateTime.now());
        familyInvitationRepository.save(invitation);
    }

    @Transactional
    public void removeMember(Long familyId, String operatorUserId, String targetUserId) {
        requireMember(familyId, operatorUserId);
        requireMember(familyId, targetUserId);
        if (familyMemberRepository.countByFamilyId(familyId) <= 1) {
            throw new IllegalArgumentException("家庭至少需要保留 1 位成员");
        }
        familyMemberRepository.deleteByFamilyIdAndUserId(familyId, targetUserId);
        appUserRepository.findByUserId(targetUserId).ifPresent(user -> {
            if (familyId.equals(user.getFamilyId())) {
                List<FamilyMember> remaining = familyMemberRepository.findByUserId(targetUserId);
                user.setFamilyId(remaining.isEmpty() ? null : remaining.get(0).getFamilyId());
                user.setUpdateTime(LocalDateTime.now());
                appUserRepository.save(user);
            }
        });
    }

    public DailyCook getOrCreateTodayCook(Long familyId, LocalDate date) {
        Optional<DailyCook> existing = dailyCookRepository.findByFamilyIdAndCookDate(familyId, date);
        if (existing.isPresent()) {
            return existing.get();
        }
        // 默认主厨：家庭第一个加入的成员
        List<AppUser> members = getFamilyMembers(familyId);
        if (members.isEmpty()) {
            throw new IllegalStateException("家庭还没有成员");
        }
        DailyCook cook = new DailyCook();
        cook.setFamilyId(familyId);
        cook.setCookUserId(members.get(0).getUserId());
        cook.setCookDate(date);
        cook.setSetAt(LocalDateTime.now());
        return dailyCookRepository.save(cook);
    }

    @Transactional
    public DailyCook switchCook(Long familyId, String userId, LocalDate date) {
        // 必须是家庭成员
        if (familyMemberRepository.findByFamilyIdAndUserId(familyId, userId).isEmpty()) {
            throw new IllegalArgumentException("你不是该家庭成员");
        }
        DailyCook cook = dailyCookRepository.findByFamilyIdAndCookDate(familyId, date)
                .orElseGet(() -> {
                    DailyCook c = new DailyCook();
                    c.setFamilyId(familyId);
                    c.setCookDate(date);
                    return c;
                });
        cook.setCookUserId(userId);
        cook.setSetAt(LocalDateTime.now());
        return dailyCookRepository.save(cook);
    }

    public List<DailyCook> getRecentCookHistory(Long familyId) {
        return dailyCookRepository.findByFamilyIdOrderByCookDateDesc(familyId);
    }

    private String generateUniqueCode() {
        for (int i = 0; i < 10; i++) {
            String code = randomCode();
            if (!familyRepository.existsByFamilyCode(code)) {
                return code;
            }
        }
        // 极端兜底：拼接时间戳后缀
        return randomCode() + (System.currentTimeMillis() % 10);
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    private void requireMember(Long familyId, String userId) {
        if (familyMemberRepository.findByFamilyIdAndUserId(familyId, userId).isEmpty()) {
            throw new IllegalArgumentException("你不是该家庭成员");
        }
    }

    private void requireCreator(Long familyId, String userId) {
        requireMember(familyId, userId);
        Family family = familyRepository.findById(familyId)
                .orElseThrow(() -> new IllegalArgumentException("家庭不存在"));
        String creatorUserId = family.getCreatorUserId();
        if (creatorUserId == null || creatorUserId.isBlank()) {
            List<FamilyMember> members = familyMemberRepository.findByFamilyId(familyId);
            creatorUserId = members.isEmpty() ? null : members.get(0).getUserId();
        }
        if (creatorUserId == null || !creatorUserId.equals(userId)) {
            throw new IllegalArgumentException("只有家庭创建人可以审核申请");
        }
    }

    private String normalizePhone(String phone) {
        String cleanPhone = phone == null ? "" : phone.trim();
        if (!cleanPhone.matches("^1\\d{10}$")) {
            throw new IllegalArgumentException("请输入正确的手机号");
        }
        return cleanPhone;
    }

    private Map<String, Object> toInvitationView(FamilyInvitation invitation) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", invitation.getId());
        view.put("familyId", invitation.getFamilyId());
        view.put("status", invitation.getStatus());
        view.put("requestType", invitation.getRequestType());
        view.put("inviteeUserId", invitation.getInviteeUserId());
        view.put("inviteePhone", invitation.getInviteePhone());
        view.put("inviterUserId", invitation.getInviterUserId());
        view.put("createTime", invitation.getCreateTime());
        familyRepository.findById(invitation.getFamilyId()).ifPresent(family -> {
            view.put("familyName", family.getName());
            view.put("familyCode", family.getFamilyCode());
        });
        appUserRepository.findByUserId(invitation.getInviterUserId()).ifPresent(user ->
                view.put("inviterName", user.getNickname() == null ? user.getPhone() : user.getNickname()));
        appUserRepository.findByUserId(invitation.getInviteeUserId()).ifPresent(user ->
                view.put("inviteeName", user.getNickname() == null ? user.getPhone() : user.getNickname()));
        return view;
    }
}
