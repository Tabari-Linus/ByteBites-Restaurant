package lii.restaurantservice.service;

import lii.restaurantservice.model.MenuItem;
import lii.restaurantservice.model.Restaurant;
import lii.restaurantservice.repository.MenuItemRepository;
import lii.restaurantservice.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public void addMenuItem(UUID restaurantId, MenuItem menuItem, String ownerId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        if (!restaurant.getOwnerId().equals(UUID.fromString(ownerId))) {
            throw new AccessDeniedException("User is not the owner of this restaurant");
        }

        menuItem.setRestaurant(restaurant);
        menuItemRepository.save(menuItem);
    }

    public List<MenuItem> getMenu(UUID restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }
}