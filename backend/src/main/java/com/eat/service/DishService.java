package com.eat.service;

import com.eat.entity.Dish;
import com.eat.repository.DishRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DishService {

    private final DishRepository dishRepository;
    private final DishCategoryService dishCategoryService;

    /** 家庭共享菜单 */
    public List<Dish> getFamilyDishes(Long familyId) {
        if (familyId == null) return List.of();
        return dishRepository.findByFamilyIdAndAvailableTrue(familyId);
    }

    public List<Dish> getFamilyDishesByMealTime(Long familyId, String mealTime) {
        if (familyId == null) return List.of();
        return dishRepository.findByFamilyIdAndMealTimeAndAvailableTrue(familyId, mealTime);
    }

    /** 历史某一天 */
    public List<Dish> getDishesByDate(Long familyId, LocalDate date) {
        if (familyId == null || date == null) return List.of();
        return dishRepository.findByFamilyIdAndCookDate(familyId, date);
    }

    /** 我的私房菜（我发布过的） */
    public List<Dish> getDishesByCook(String cookUserId) {
        return dishRepository.findByCookUserId(cookUserId);
    }

    public Dish getDishById(Long id) {
        return dishRepository.findById(id).orElse(null);
    }

    public Dish createDish(Dish dish) {
        if (dish.getPrice() == null) {
            dish.setPrice(BigDecimal.ZERO);
        }
        if (dish.getFamilyId() == null) {
            throw new IllegalArgumentException("请先登录后发布");
        }
        if (dish.getCookDate() == null) {
            dish.setCookDate(LocalDate.now());
        }
        if (dish.getAvailable() == null) {
            dish.setAvailable(true);
        }
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        dishCategoryService.ensureCategory(dish.getCategory());
        return dishRepository.save(dish);
    }

    public Dish updateDish(Long id, Dish dishDetails) {
        Dish dish = dishRepository.findById(id).orElse(null);
        if (dish != null) {
            dish.setName(dishDetails.getName());
            dish.setDescription(dishDetails.getDescription());
            dish.setPrice(dishDetails.getPrice());
            dish.setImage(dishDetails.getImage());
            dish.setCategory(dishDetails.getCategory());
            dish.setAvailable(dishDetails.getAvailable());
            dish.setUpdateTime(LocalDateTime.now());
            return dishRepository.save(dish);
        }
        return null;
    }

    public void deleteDish(Long id) {
        dishRepository.deleteById(id);
    }
}
