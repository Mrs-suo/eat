package com.eat.repository;

import com.eat.entity.DishEditRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishEditRequestRepository extends JpaRepository<DishEditRequest, Long> {
    List<DishEditRequest> findByUserId(String userId);
    List<DishEditRequest> findByStatus(Integer status);
    List<DishEditRequest> findByFamilyIdAndStatus(Long familyId, Integer status);
    List<DishEditRequest> findByFamilyId(Long familyId);
}
