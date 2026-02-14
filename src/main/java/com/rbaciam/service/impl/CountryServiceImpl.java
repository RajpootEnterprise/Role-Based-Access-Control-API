package com.rbaciam.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rbaciam.dto.CountryResponse;
import com.rbaciam.entity.Country;
import com.rbaciam.entity.User;
import com.rbaciam.exception.NotFoundException;
import com.rbaciam.repository.CountryRepository;
import com.rbaciam.repository.UserRepository;
import com.rbaciam.service.CountryService;

@Service
public class CountryServiceImpl implements CountryService {

    @Autowired
    private CountryRepository countryRepository;
    
    @Autowired
    private UserRepository userRepository;
    

    @Override
    public List<CountryResponse> getAllCountries(Long userId) {
  
        List<Country> countries = countryRepository.findAll();
     
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    	 
        return countries.stream()
                .map(country -> new CountryResponse(
                        country.getName(),
                        country.getOfficialName(),
                        country.getRegion()))
                .collect(Collectors.toList());
    }
}