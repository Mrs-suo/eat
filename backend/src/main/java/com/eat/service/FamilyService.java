package com.eat.service;

import com.eat.entity.AppUser;
import com.eat.entity.DailyCook;
import com.eat.entity.Family;
import com.eat.entity.FamilyMember;
import com.eat.repository.AppUserRepository;
import com.eat.repository.DailyCookRepository;
import com.eat.repository.FamilyMemberRepository;
import com.eat.repository.FamilyRepository;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FamilyService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int MAX_FAMILY_MEMBERS = 2;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final AppUserRepository appUserRepository;
    private final DailyCookRepository dailyCookRepository;

    @Transactional
    public Family createFamily(String familyName, AppUser creator) {
        if (creator.getFamilyId() != null) {
            throw new IllegalArgumentException("你已属于一个家庭，不能再创建");
        }
        Family family = new Family();
        family.setName(familyName);
        family.setFamilyCode(generateUniqueCode());
        family.setCreateTime(LocalDateTime.now());
        family = familyRepository.save(family);

        addMember(family.getId(), creator.getUserId());
        creator.setFamilyId(family.getId());
        creator.setUpdateTime(LocalDateTime.now());
        appUserRepository.save(creator);
        return family;
    }

    @Transactional
    public Family joinFamily(String familyCode, AppUser user) {
        if (user.getFamilyId() != null) {
            throw new IllegalArgumentException("你已属于一个家庭，请先退出");
        }
        Family family = familyRepository.findByFamilyCode(familyCode.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("家庭码无效"));
        long currentCount = familyMemberRepository.countByFamilyId(family.getId());
        if (currentCount >= MAX_FAMILY_MEMBERS) {
            throw new IllegalArgumentException("家庭成员已满（最多 " + MAX_FAMILY_MEMBERS + " 人）");
        }
        addMember(family.getId(), user.getUserId());
        user.setFamilyId(family.getId());
        user.setUpdateTime(LocalDateTime.now());
        appUserRepository.save(user);
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
}
