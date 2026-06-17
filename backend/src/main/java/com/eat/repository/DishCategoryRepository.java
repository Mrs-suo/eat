package com.eat.repository;

import com.eat.entity.DishCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DishCategoryRepository extends JpaRepository<DishCategory, Long> {
    List<DishCategory> findByEnabledTrueOrderBySortOrderAscIdAsc();
    Optional<DishCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
