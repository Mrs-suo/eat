package com.eat.repository;

import com.eat.entity.AppUser;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByPhone(String phone);
    Optional<AppUser> findByUserId(String userId);
    List<AppUser> findByFamilyId(Long familyId);
}
