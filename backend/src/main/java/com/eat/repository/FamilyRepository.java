package com.eat.repository;

import com.eat.entity.Family;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FamilyRepository extends JpaRepository<Family, Long> {
    Optional<Family> findByFamilyCode(String familyCode);
    boolean existsByFamilyCode(String familyCode);
}
