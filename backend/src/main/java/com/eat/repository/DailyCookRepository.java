package com.eat.repository;

import com.eat.entity.DailyCook;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyCookRepository extends JpaRepository<DailyCook, Long> {
    Optional<DailyCook> findByFamilyIdAndCookDate(Long familyId, LocalDate cookDate);
    List<DailyCook> findByFamilyIdAndCookDateBetween(Long familyId, LocalDate start, LocalDate end);
    List<DailyCook> findByFamilyIdOrderByCookDateDesc(Long familyId);
}
