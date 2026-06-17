package com.eat.service;

import com.eat.entity.DishCategory;
import com.eat.repository.DishCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DishCategoryService {

    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList("主食", "小吃", "饮品", "甜点");

    private final DishCategoryRepository dishCategoryRepository;

    @Transactional(readOnly = true)
    public List<DishCategory> getAllCategories() {
        return dishCategoryRepository.findByEnabledTrueOrderBySortOrderAscIdAsc();
    }

    @Transactional
    public List<DishCategory> getOrCreateDefaultCategories() {
        ensureDefaults();
        return getAllCategories();
    }

    @Transactional
    public DishCategory createCategory(DishCategory category) {
        String name = normalize(category.getName());
        if (name.isEmpty()) {
            throw new IllegalArgumentException("分类名称不能为空");
        }
        return ensureCategory(name);
    }

    @Transactional
    public DishCategory ensureCategory(String categoryName) {
        String name = normalize(categoryName);
        if (name.isEmpty()) {
            return null;
        }

        return dishCategoryRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    DishCategory category = new DishCategory();
                    category.setName(name);
                    category.setSortOrder((int) dishCategoryRepository.count() + 1);
                    category.setEnabled(true);
                    return dishCategoryRepository.save(category);
                });
    }

    private void ensureDefaults() {
        DEFAULT_CATEGORIES.forEach(category -> {
            if (!dishCategoryRepository.existsByNameIgnoreCase(category)) {
                DishCategory dishCategory = new DishCategory();
                dishCategory.setName(category);
                dishCategory.setSortOrder(DEFAULT_CATEGORIES.indexOf(category) + 1);
                dishCategory.setEnabled(true);
                dishCategoryRepository.save(dishCategory);
            }
        });
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
