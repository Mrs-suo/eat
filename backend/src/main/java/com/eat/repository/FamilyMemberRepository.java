package com.eat.repository;

import com.eat.entity.FamilyMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {
    List<FamilyMember> findByFamilyId(Long familyId);
    List<FamilyMember> findByUserId(String userId);
    Optional<FamilyMember> findByFamilyIdAndUserId(Long familyId, String userId);
    long countByFamilyId(Long familyId);
    void deleteByFamilyIdAndUserId(Long familyId, String userId);
}
