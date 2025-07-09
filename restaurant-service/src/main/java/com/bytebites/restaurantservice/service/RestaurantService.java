package com.bytebites.restaurantservice.service;

import com.bytebites.restaurantservice.dto.CreateRestaurantRequest;
import com.bytebites.restaurantservice.dto.RestaurantResponse;
import com.bytebites.restaurantservice.dto.UpdateRestaurantRequest;
import com.bytebites.restaurantservice.enums.RestaurantStatus;
import com.bytebites.restaurantservice.event.RestaurantEventPublisher;
import com.bytebites.restaurantservice.exception.RestaurantNotFoundException;
import com.bytebites.restaurantservice.exception.UnauthorizedOperationException;
import com.bytebites.restaurantservice.mapper.RestaurantMapper;
import com.bytebites.restaurantservice.model.Restaurant;
import com.bytebites.restaurantservice.repository.RestaurantRepository;
import com.bytebites.restaurantservice.security.SecurityService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RestaurantService {

    private static final Logger logger = LoggerFactory.getLogger(RestaurantService.class);

    private final RestaurantRepository restaurantRepository;
    private final RestaurantMapper restaurantMapper;
    private final SecurityService securityService;
    private final RestaurantEventPublisher restaurantEventPublisher;

    public RestaurantService(RestaurantRepository restaurantRepository,
                             RestaurantMapper restaurantMapper,
                             SecurityService securityService,
                             RestaurantEventPublisher restaurantEventPublisher) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantMapper = restaurantMapper;
        this.securityService = securityService;
        this.restaurantEventPublisher = restaurantEventPublisher;
    }

    @PreAuthorize("hasRole('RESTAURANT_OWNER') or hasRole('ADMIN')")
    public RestaurantResponse createRestaurant(CreateRestaurantRequest request, UUID ownerId) {
        logger.info("Creating restaurant: {} for owner: {}", request.name(), ownerId);

        
        if (restaurantRepository.existsByOwnerIdAndName(ownerId, request.name())) {
            throw new RuntimeException("Restaurant with name '" + request.name() + "' already exists");
        }

        Restaurant restaurant = restaurantMapper.toEntity(request, ownerId);
        restaurant.setStatus(RestaurantStatus.ACTIVE);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        restaurantEventPublisher.publishRestaurantCreatedEvent(savedRestaurant);

        logger.info("Restaurant created successfully with ID: {}", savedRestaurant.getId());
        return restaurantMapper.toResponse(savedRestaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getAllActiveRestaurants() {
        logger.info("Fetching all active restaurants");

        List<Restaurant> restaurants = restaurantRepository.findActiveRestaurantsOrderByCreatedAt(RestaurantStatus.ACTIVE);
        return restaurantMapper.toResponseList(restaurants);
    }

    @Transactional(readOnly = true)
    public Page<RestaurantResponse> getAllRestaurants(Pageable pageable) {
        logger.info("Fetching all restaurants with pagination");

        Page<Restaurant> restaurants = restaurantRepository.findAll(pageable);
        return restaurants.map(restaurantMapper::toResponseWithoutMenuItems);
    }

    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurantById(UUID id) {
        logger.info("Fetching restaurant by ID: {}", id);

        Restaurant restaurant = restaurantRepository.findByIdWithMenuItems(id)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with ID: " + id));

        return restaurantMapper.toResponse(restaurant);
    }

    @Transactional(readOnly = true)
    public List<RestaurantResponse> getMyRestaurants(UUID ownerId) {
        logger.info("Fetching restaurants for owner: {}", ownerId);

        List<Restaurant> restaurants = restaurantRepository.findByOwnerId(ownerId);
        return restaurantMapper.toResponseList(restaurants);
    }

    public RestaurantResponse updateRestaurant(UUID id, UpdateRestaurantRequest request, UUID currentUserId) {
        logger.info("Updating restaurant: {} by user: {}", id, currentUserId);

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with ID: " + id));

        
        if (!securityService.isOwnerOrAdmin(currentUserId, restaurant.getOwnerId())) {
            throw new UnauthorizedOperationException("You are not authorized to update this restaurant");
        }

        restaurantMapper.updateEntityFromRequest(request, restaurant);
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);

        logger.info("Restaurant updated successfully: {}", id);
        return restaurantMapper.toResponse(updatedRestaurant);
    }

    public void deleteRestaurant(UUID id, UUID currentUserId) {
        logger.info("Deleting restaurant: {} by user: {}", id, currentUserId);

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException("Restaurant not found with ID: " + id));

        
        if (!securityService.isOwnerOrAdmin(currentUserId, restaurant.getOwnerId())) {
            throw new UnauthorizedOperationException("You are not authorized to delete this restaurant");
        }

        
        restaurant.setStatus(RestaurantStatus.INACTIVE);
        restaurantRepository.save(restaurant);

        logger.info("Restaurant soft deleted successfully: {}", id);
    }
}