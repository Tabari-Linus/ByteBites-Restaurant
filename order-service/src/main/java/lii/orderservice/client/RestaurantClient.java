package lii.orderservice.client;

import lii.orderservice.dto.MenuItemResponse;
import lii.orderservice.dto.RestaurantResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RestaurantClient {

    private final WebClient.Builder webClientBuilder;
    private static final String RESTAURANT_SERVICE_URL = "http://restaurant-service";

    public Mono<RestaurantResponse> getRestaurantById(UUID restaurantId) {
        return webClientBuilder.build()
                .get()
                .uri(RESTAURANT_SERVICE_URL + "/api/restaurants/{restaurantId}", restaurantId)
                .retrieve()
                .bodyToMono(RestaurantResponse.class);
    }

    public Mono<MenuItemResponse> getMenuItemById(UUID menuItemId) {
        return webClientBuilder.build()
                .get()
                .uri(RESTAURANT_SERVICE_URL + "/api/restaurants/menu-items/{menuItemId}", menuItemId)
                .retrieve()
                .bodyToMono(MenuItemResponse.class);
    }
}