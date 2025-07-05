package lii.restaurantservice.repository;

import lii.restaurantservice.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {}