package com.eat.repository;

import com.eat.entity.FamilyInvitation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyInvitationRepository extends JpaRepository<FamilyInvitation, Long> {
    List<FamilyInvitation> findByFamilyIdOrderByCreateTimeDesc(Long familyId);
    List<FamilyInvitation> findByInviteeUserIdAndStatusOrderByCreateTimeDesc(String inviteeUserId, String status);
    Optional<FamilyInvitation> findByFamilyIdAndInviteeUserIdAndStatus(Long familyId, String inviteeUserId, String status);
}
