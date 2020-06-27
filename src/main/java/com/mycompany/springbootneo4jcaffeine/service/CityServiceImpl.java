package com.mycompany.springbootneo4jcaffeine.service;

import com.mycompany.springbootneo4jcaffeine.exception.CityNotFoundException;
import com.mycompany.springbootneo4jcaffeine.model.City;
import com.mycompany.springbootneo4jcaffeine.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CityServiceImpl implements CityService {

    private final CityRepository cityRepository;

    @Override
    public City saveCity(City city) {
        return cityRepository.save(city);
    }

    @Override
    public Page<City> getCities(Pageable pageable) {
        return cityRepository.findAll(pageable);
    }

    @Override
    public void deleteCity(City city) {
        cityRepository.delete(city);
    }

    @Override
    public City validateAndGetCity(String cityId) {
        return cityRepository.findById(cityId).orElseThrow(() -> new CityNotFoundException(cityId));
    }

}
