package com.eat.service;

import com.eat.entity.AppUser;
import com.eat.entity.Family;
import com.eat.entity.FamilyMember;
import com.eat.repository.AppUserRepository;
import com.eat.repository.FamilyMemberRepository;
import com.eat.repository.FamilyRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final FamilyRepository familyRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final FamilyService familyService;

    public AppUser login(String phone) {
        String cleanPhone = normalizePhone(phone);
        AppUser user = appUserRepository.findByPhone(cleanPhone)
                .orElseThrow(() -> new IllegalArgumentException("该手机号未注册，请先注册"));
        ensureCurrentFamily(user);
        return user;
    }

    @Transactional
    public RegisterResult register(String phone, String nickname, String familyCode, String familyName) {
        String cleanPhone = normalizePhone(phone);
        if (appUserRepository.findByPhone(cleanPhone).isPresent()) {
            throw new IllegalArgumentException("该手机号已注册，请直接登录");
        }
        String cleanNickname = (nickname == null || nickname.trim().isEmpty())
                ? "家人" + cleanPhone.substring(7)
                : nickname.trim();
        AppUser user = new AppUser();
        user.setPhone(cleanPhone);
        user.setUserId("u_" + cleanPhone);
        user.setNickname(cleanNickname);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user = appUserRepository.save(user);

        Family family = null;
        if (familyName != null && !familyName.trim().isEmpty()) {
            family = familyService.createFamily(familyName.trim(), user);
        } else if (familyCode != null && !familyCode.trim().isEmpty()) {
            familyService.requestJoinFamily(familyCode.trim(), user);
        }
        return new RegisterResult(user, family);
    }

    public AppUser getByUserId(String userId) {
        return appUserRepository.findByUserId(userId).orElse(null);
    }

    public Family getCurrentFamily(String userId) {
        AppUser user = getByUserId(userId);
        if (user == null) return null;
        ensureCurrentFamily(user);
        if (user.getFamilyId() == null) return null;
        return familyRepository.findById(user.getFamilyId()).orElse(null);
    }

    @Transactional
    public AppUser updateProfile(String userId, String nickname, String avatar) {
        AppUser user = appUserRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        String cleanNickname = nickname == null ? "" : nickname.trim();
        if (cleanNickname.isEmpty()) {
            throw new IllegalArgumentException("请输入昵称");
        }
        if (cleanNickname.length() > 32) {
            throw new IllegalArgumentException("昵称最多 32 个字符");
        }
        user.setNickname(cleanNickname);
        user.setAvatar(normalizeText(avatar));
        user.setUpdateTime(LocalDateTime.now());
        return appUserRepository.save(user);
    }

    public void syncUserStorage(AppUser user) {
        // 这里保留一个钩子，登录后由前端再写 storage
    }

    private String normalizePhone(String phone) {
        String cleanPhone = phone == null ? "" : phone.trim();
        if (!cleanPhone.matches("^1\\d{10}$")) {
            throw new IllegalArgumentException("请输入正确的手机号");
        }
        return cleanPhone;
    }

    private String normalizeText(String value) {
        if (value == null) return null;
        String cleanValue = value.trim();
        return cleanValue.isEmpty() ? null : cleanValue;
    }

    private void ensureCurrentFamily(AppUser user) {
        if (user.getFamilyId() != null
                && familyMemberRepository.findByFamilyIdAndUserId(user.getFamilyId(), user.getUserId()).isPresent()) {
            return;
        }
        List<FamilyMember> memberships = familyMemberRepository.findByUserId(user.getUserId());
        user.setFamilyId(memberships.isEmpty() ? null : memberships.get(0).getFamilyId());
        user.setUpdateTime(LocalDateTime.now());
        appUserRepository.save(user);
    }

    public record RegisterResult(AppUser user, Family family) {}
}
