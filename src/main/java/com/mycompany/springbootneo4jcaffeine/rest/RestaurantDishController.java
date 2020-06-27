package com.mycompany.springbootneo4jcaffeine.rest;

import com.mycompany.springbootneo4jcaffeine.exception.DishNotFoundException;
import com.mycompany.springbootneo4jcaffeine.mapper.DishMapper;
import com.mycompany.springbootneo4jcaffeine.model.Dish;
import com.mycompany.springbootneo4jcaffeine.model.Restaurant;
import com.mycompany.springbootneo4jcaffeine.rest.dto.CreateDishDto;
import com.mycompany.springbootneo4jcaffeine.rest.dto.DishDto;
import com.mycompany.springbootneo4jcaffeine.rest.dto.RestaurantMenu;
import com.mycompany.springbootneo4jcaffeine.rest.dto.UpdateDishDto;
import com.mycompany.springbootneo4jcaffeine.service.DishService;
import com.mycompany.springbootneo4jcaffeine.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.mycompany.springbootneo4jcaffeine.config.CacheConfig.DISHES;
import static com.mycompany.springbootneo4jcaffeine.config.CacheConfig.RESTAURANTS;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/dishes")
public class RestaurantDishController {

    private final RestaurantService restaurantService;
    private final DishService dishService;
    private final DishMapper dishMapper;

    @Cacheable(cacheNames = DISHES, key = "{#restaurantId,#dishId}")
    @GetMapping("/{dishId}")
    public DishDto getRestaurantDish(@PathVariable String restaurantId, @PathVariable String dishId) {
        Restaurant restaurant = restaurantService.validateAndGetRestaurant(restaurantId);
        Dish dish = restaurantService.validateAndGetDish(restaurant, dishId);
        return dishMapper.toDishDto(dish);
    }

    @Cacheable(cacheNames = DISHES, key = "#restaurantId")
    @GetMapping
    public RestaurantMenu getRestaurantDishes(@PathVariable String restaurantId) {
        Restaurant restaurant = restaurantService.validateAndGetRestaurant(restaurantId);

        RestaurantMenu restaurantMenu = new RestaurantMenu();
        restaurant.getDishes().forEach(dish -> restaurantMenu.getDishes().add(dishMapper.toDishDto(dish)));
        return restaurantMenu;
    }

    @Caching(
            put = @CachePut(cacheNames = DISHES, key = "{#restaurantId,#result.id}"),
            evict = {
                    @CacheEvict(cacheNames = DISHES, key = "#restaurantId"),
                    @CacheEvict(cacheNames = RESTAURANTS, key = "#restaurantId")
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public DishDto createRestaurantDish(@PathVariable String restaurantId, @Valid @RequestBody CreateDishDto createDishDto) {
        Restaurant restaurant = restaurantService.validateAndGetRestaurant(restaurantId);
        Dish dish = dishMapper.toDish(createDishDto);
        dish = dishService.saveDish(dish);

        restaurant.getDishes().add(dish);
        restaurantService.saveRestaurant(restaurant);
        return dishMapper.toDishDto(dish);
    }

    @Caching(
            put = @CachePut(cacheNames = DISHES, key = "{#restaurantId,#dishId}"),
            evict = {
                    @CacheEvict(cacheNames = DISHES, key = "#restaurantId"),
                    @CacheEvict(cacheNames = RESTAURANTS, key = "#restaurantId")
            }
    )
    @PutMapping("/{dishId}")
    public DishDto updateRestaurantDish(@PathVariable String restaurantId, @PathVariable String dishId,
                                        @Valid @RequestBody UpdateDishDto updateDishDto) {
        Restaurant restaurant = restaurantService.validateAndGetRestaurant(restaurantId);
        Dish dish = restaurantService.validateAndGetDish(restaurant, dishId);

        dishMapper.updateDishFromDto(updateDishDto, dish);
        dish = dishService.saveDish(dish);
        return dishMapper.toDishDto(dish);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = DISHES, key = "{#restaurantId,#dishId}"),
            @CacheEvict(cacheNames = DISHES, key = "#restaurantId"),
            @CacheEvict(cacheNames = RESTAURANTS, key = "#restaurantId")
    })
    @DeleteMapping("/{dishId}")
    public DishDto deleteRestaurantDish(@PathVariable String restaurantId, @PathVariable String dishId) {
        Restaurant restaurant = restaurantService.validateAndGetRestaurant(restaurantId);
        Dish dish = restaurant.getDishes().stream()
                .filter(m -> m.getId().equals(dishId)).findFirst().orElseThrow(() -> new DishNotFoundException(dishId));
        dishService.deleteDish(dish);
        return dishMapper.toDishDto(dish);
    }

}
