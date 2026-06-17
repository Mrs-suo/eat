package com.eat.controller;

import com.eat.entity.DishCategory;
import com.eat.service.DishCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DishCategoryController {

    private final DishCategoryService dishCategoryService;

    @GetMapping
    public List<DishCategory> getCategories() {
        return dishCategoryService.getOrCreateDefaultCategories();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DishCategory createCategory(@RequestBody DishCategory category) {
        return dishCategoryService.createCategory(category);
    }
}
