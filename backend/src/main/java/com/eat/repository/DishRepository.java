package com.eat.repository;

import com.eat.entity.Dish;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findByCategory(String category);
    List<Dish> findByAvailableTrue();
    List<Dish> findByFamilyIdAndAvailableTrue(Long familyId);
    List<Dish> findByFamilyIdAndMealTimeAndAvailableTrue(Long familyId, String mealTime);
    List<Dish> findByFamilyIdAndCookDate(Long familyId, LocalDate cookDate);
    List<Dish> findByFamilyIdAndCookUserId(Long familyId, String cookUserId);
    List<Dish> findByCookUserId(String cookUserId);
    List<Dish> findByMealTimeAndAvailableTrue(String mealTime);
}
