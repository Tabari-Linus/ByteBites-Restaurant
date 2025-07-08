package com.bytebites.orderservice.repository;

import com.bytebites.orderservice.enums.OrderStatus;
import com.bytebites.orderservice.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<Order> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);

    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);

    Page<Order> findByRestaurantId(UUID restaurantId, Pageable pageable);

    List<Order> findByRestaurantIdAndStatus(UUID restaurantId, OrderStatus status);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") UUID id);

    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId AND o.id = :id")
    Optional<Order> findByIdAndCustomerId(@Param("id") UUID id, @Param("customerId") UUID customerId);

    @Query("SELECT o FROM Order o WHERE o.restaurantId = :restaurantId AND o.id = :id")
    Optional<Order> findByIdAndRestaurantId(@Param("id") UUID id, @Param("restaurantId") UUID restaurantId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.restaurantId = :restaurantId AND o.status = :status")
    Long countByRestaurantIdAndStatus(@Param("restaurantId") UUID restaurantId, @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}
