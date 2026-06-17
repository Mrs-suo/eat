package com.eat.service;

import com.eat.entity.AppUser;
import com.eat.entity.Family;
import com.eat.entity.FamilyMember;
import com.eat.repository.AppUserRepository;
import com.eat.repository.FamilyMemberRepository;
import com.eat.repository.FamilyRepository;
import java.time.LocalDateTime;
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
        // 注册时已加入家庭，登录时校验一致性
        if (user.getFamilyId() == null) {
            throw new IllegalStateException("账号数据异常：未关联家庭");
        }
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
        boolean joining = familyCode != null && !familyCode.trim().isEmpty();
        boolean creating = familyName != null && !familyName.trim().isEmpty();
        if (joining == creating) {
            throw new IllegalArgumentException("请选择创建家庭或加入家庭");
        }

        AppUser user = new AppUser();
        user.setPhone(cleanPhone);
        user.setUserId("u_" + cleanPhone);
        user.setNickname(cleanNickname);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user = appUserRepository.save(user);

        Family family;
        if (creating) {
            family = familyService.createFamily(familyName.trim(), user);
        } else {
            family = familyService.joinFamily(familyCode.trim(), user);
        }
        return new RegisterResult(user, family);
    }

    public AppUser getByUserId(String userId) {
        return appUserRepository.findByUserId(userId).orElse(null);
    }

    public Family getCurrentFamily(String userId) {
        AppUser user = getByUserId(userId);
        if (user == null || user.getFamilyId() == null) return null;
        return familyRepository.findById(user.getFamilyId()).orElse(null);
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

    public record RegisterResult(AppUser user, Family family) {}
}
