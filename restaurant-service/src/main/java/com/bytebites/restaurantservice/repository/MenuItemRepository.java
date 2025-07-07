package com.bytebites.restaurantservice.repository;

import com.bytebites.restaurantservice.model.MenuItem;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    List<MenuItem> findByRestaurantIdOrderByCategory(UUID restaurantId);

    List<MenuItem> findByRestaurantIdAndAvailable(UUID restaurantId, Boolean available);

    @Query("SELECT m FROM MenuItem m WHERE m.restaurant.id = :restaurantId AND m.category = :category")
    List<MenuItem> findByRestaurantIdAndCategory(@Param("restaurantId") UUID restaurantId,
                                                 @Param("category") String category);

    Optional<MenuItem> findByIdAndRestaurantId(UUID id, UUID restaurantId);
}