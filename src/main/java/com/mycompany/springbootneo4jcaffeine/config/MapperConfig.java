package com.mycompany.springbootneo4jcaffeine.config;

import com.mycompany.springbootneo4jcaffeine.dto.CreateCityDto;
import com.mycompany.springbootneo4jcaffeine.dto.CreateMealDto;
import com.mycompany.springbootneo4jcaffeine.dto.CreateRestaurantDto;
import com.mycompany.springbootneo4jcaffeine.dto.ResponseCityDto;
import com.mycompany.springbootneo4jcaffeine.dto.ResponseMealDto;
import com.mycompany.springbootneo4jcaffeine.dto.ResponseRestaurantDto;
import com.mycompany.springbootneo4jcaffeine.dto.UpdateCityDto;
import com.mycompany.springbootneo4jcaffeine.dto.UpdateMealDto;
import com.mycompany.springbootneo4jcaffeine.dto.UpdateRestaurantDto;
import com.mycompany.springbootneo4jcaffeine.exception.CityNotFoundException;
import com.mycompany.springbootneo4jcaffeine.model.City;
import com.mycompany.springbootneo4jcaffeine.model.Meal;
import com.mycompany.springbootneo4jcaffeine.model.Restaurant;
import com.mycompany.springbootneo4jcaffeine.service.CityService;
import ma.glasnost.orika.CustomMapper;
import ma.glasnost.orika.MapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.impl.DefaultMapperFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class MapperConfig {

    private final CityService cityService;

    public MapperConfig(CityService cityService) {
        this.cityService = cityService;
    }

    @Bean
    MapperFactory mapperFactory() {
        DefaultMapperFactory defaultMapperFactory = new DefaultMapperFactory.Builder().useAutoMapping(true).build();

        // ---
        // City

        defaultMapperFactory.classMap(CreateCityDto.class, City.class).byDefault().register();
        defaultMapperFactory.classMap(UpdateCityDto.class, City.class).mapNulls(false).byDefault().register();
        defaultMapperFactory.classMap(City.class, ResponseCityDto.class).byDefault().register();

        // ---
        // Restaurant

        defaultMapperFactory.classMap(CreateRestaurantDto.class, Restaurant.class).byDefault()
                .customize(new CustomMapper<CreateRestaurantDto, Restaurant>() {
                    @Override
                    public void mapAtoB(CreateRestaurantDto createRestaurantDto, Restaurant restaurant, MappingContext context) {
                        super.mapAtoB(createRestaurantDto, restaurant, context);

                        try {
                            City city = cityService.validateAndGetCityById(createRestaurantDto.getCityId());
                            restaurant.setCity(city);
                        } catch (CityNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                })
                .register();

        defaultMapperFactory.classMap(UpdateRestaurantDto.class, Restaurant.class).mapNulls(false).byDefault()
                .customize(new CustomMapper<UpdateRestaurantDto, Restaurant>() {
                    @Override
                    public void mapAtoB(UpdateRestaurantDto updateRestaurantDto, Restaurant restaurant, MappingContext context) {
                        super.mapAtoB(updateRestaurantDto, restaurant, context);

                        String newCityId = updateRestaurantDto.getCityId();
                        if (!StringUtils.isEmpty(newCityId)) {
                            try {
                                City city = cityService.validateAndGetCityById(newCityId);
                                restaurant.setCity(city);
                            } catch (CityNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                })
                .register();

        defaultMapperFactory.classMap(Restaurant.class, ResponseRestaurantDto.class).byDefault().register();

        // ---
        // Meal

        defaultMapperFactory.classMap(CreateMealDto.class, Meal.class).byDefault().register();
        defaultMapperFactory.classMap(UpdateMealDto.class, Meal.class).mapNulls(false).byDefault().register();
        defaultMapperFactory.classMap(Meal.class, ResponseMealDto.class).byDefault().register();

        return defaultMapperFactory;
    }

    @Bean
    MapperFacade mapperFacade() {
        return mapperFactory().getMapperFacade();
    }

}
