package com.eat.controller;

import com.eat.entity.Dish;
import com.eat.service.DishService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DishController {

    private final DishService dishService;

    /** 家庭共享菜单 */
    @GetMapping
    public List<Dish> getFamilyDishes(@RequestParam(required = false) Long familyId) {
        return dishService.getFamilyDishes(familyId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dish> getDishById(@PathVariable Long id) {
        Dish dish = dishService.getDishById(id);
        if (dish != null) {
            return ResponseEntity.ok(dish);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/meal-time/{mealTime}")
    public List<Dish> getDishesByMealTime(
            @PathVariable String mealTime,
            @RequestParam(required = false) Long familyId
    ) {
        return dishService.getFamilyDishesByMealTime(familyId, mealTime);
    }

    @GetMapping("/by-date")
    public List<Dish> getDishesByDate(
            @RequestParam Long familyId,
            @RequestParam String date
    ) {
        return dishService.getDishesByDate(familyId, LocalDate.parse(date));
    }

    @GetMapping("/by-cook/{cookUserId}")
    public List<Dish> getDishesByCook(@PathVariable String cookUserId) {
        return dishService.getDishesByCook(cookUserId);
    }

    @PostMapping
    public ResponseEntity<?> createDish(@RequestBody Dish dish) {
        try {
            return ResponseEntity.ok(dishService.createDish(dish));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Dish> updateDish(@PathVariable Long id, @RequestBody Dish dish) {
        Dish updated = dishService.updateDish(id, dish);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDish(@PathVariable Long id) {
        dishService.deleteDish(id);
        return ResponseEntity.ok().build();
    }
}
