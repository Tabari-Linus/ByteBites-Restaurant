package com.bytebites.restaurantservice.repository;

import com.bytebites.restaurantservice.model.Restaurant;
import com.bytebites.restaurantservice.enums.RestaurantStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    List<Restaurant> findByOwnerId(UUID ownerId);

    Page<Restaurant> findByStatus(RestaurantStatus status, Pageable pageable);

    @Query("SELECT r FROM Restaurant r WHERE r.status = :status ORDER BY r.createdAt DESC")
    List<Restaurant> findActiveRestaurantsOrderByCreatedAt(@Param("status") RestaurantStatus status);

    @Query("SELECT r FROM Restaurant r LEFT JOIN FETCH r.menuItems WHERE r.id = :id")
    Optional<Restaurant> findByIdWithMenuItems(@Param("id") UUID id);

    boolean existsByOwnerIdAndName(UUID ownerId, String name);
}