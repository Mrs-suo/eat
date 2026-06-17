package com.eat.service;

import com.eat.entity.Dish;
import com.eat.entity.DishEditRequest;
import com.eat.repository.DishEditRequestRepository;
import com.eat.repository.DishRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DishEditRequestService {

    private final DishEditRequestRepository requestRepository;
    private final DishRepository dishRepository;
    private final NotificationPushService notificationPushService;

    public List<DishEditRequest> getPendingByFamily(Long familyId) {
        if (familyId == null) return List.of();
        return requestRepository.findByFamilyIdAndStatus(familyId, 0);
    }

    public List<DishEditRequest> getAllByFamily(Long familyId) {
        if (familyId == null) return List.of();
        return requestRepository.findByFamilyId(familyId);
    }

    public List<DishEditRequest> getRequestsByUserId(String userId) {
        return requestRepository.findByUserId(userId);
    }

    public DishEditRequest createRequest(DishEditRequest request) {
        if (request.getFamilyId() == null) {
            throw new IllegalArgumentException("缺少家庭信息");
        }
        request.setStatus(0);
        request.setCreateTime(LocalDateTime.now());
        DishEditRequest saved = requestRepository.save(request);
        notificationPushService.refreshFamily(saved.getFamilyId());
        return saved;
    }

    public DishEditRequest approveRequest(Long requestId) {
        DishEditRequest request = requestRepository.findById(requestId).orElse(null);
        if (request == null) return null;

        request.setStatus(1);
        request.setUpdateTime(LocalDateTime.now());

        if (request.getOriginalDish() != null) {
            Dish dish = request.getOriginalDish();
            dish.setName(request.getName());
            dish.setDescription(request.getDescription());
            dish.setPrice(request.getPrice());
            dish.setImage(request.getImage());
            dish.setCategory(request.getCategory());
            dish.setMealTime(request.getMealTime());
            dish.setUpdateTime(LocalDateTime.now());
            dishRepository.save(dish);
        } else {
            Dish newDish = new Dish();
            newDish.setFamilyId(request.getFamilyId());
            newDish.setName(request.getName());
            newDish.setDescription(request.getDescription());
            newDish.setPrice(request.getPrice());
            newDish.setImage(request.getImage());
            newDish.setCategory(request.getCategory());
            newDish.setMealTime(request.getMealTime());
            newDish.setCookUserId(request.getUserId());
            newDish.setCreatedByUserId(request.getUserId());
            newDish.setAvailable(true);
            newDish.setCreateTime(LocalDateTime.now());
            newDish.setUpdateTime(LocalDateTime.now());
            dishRepository.save(newDish);
        }

        DishEditRequest saved = requestRepository.save(request);
        notificationPushService.refreshFamily(saved.getFamilyId());
        notificationPushService.refreshUser(saved.getUserId());
        return saved;
    }

    public DishEditRequest rejectRequest(Long requestId, String reason) {
        DishEditRequest request = requestRepository.findById(requestId).orElse(null);
        if (request == null) return null;

        request.setStatus(2);
        request.setRejectReason(reason);
        request.setUpdateTime(LocalDateTime.now());
        DishEditRequest saved = requestRepository.save(request);
        notificationPushService.refreshFamily(saved.getFamilyId());
        notificationPushService.refreshUser(saved.getUserId());
        return saved;
    }

    public void deleteRequest(Long id) {
        requestRepository.deleteById(id);
    }
}
