package com.rbaciam.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rbaciam.dto.TimezoneResponse;
import com.rbaciam.entity.User;
import com.rbaciam.exception.NotFoundException;
import com.rbaciam.repository.TimezoneRepository;
import com.rbaciam.repository.UserRepository;
import com.rbaciam.service.TimezoneService;

@Service
public class TimezoneServiceImpl implements TimezoneService {

    private final TimezoneRepository timezoneRepository;
    
    @Autowired
    private UserRepository userRepository;
   

    public TimezoneServiceImpl(TimezoneRepository timezoneRepository) {
        this.timezoneRepository = timezoneRepository;
    }

    @Override
    public List<TimezoneResponse> getAllTimezones(Long userId) {
    	
    	  User user = userRepository.findByIdAndDeletedAtIsNull(userId)
	                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    	 
        return timezoneRepository.findAll()
                .stream()
                .map(timezone -> new TimezoneResponse(
                        timezone.getName(),
                        timezone.getUtcOffset(),
                        timezone.getDescription()))
                .collect(Collectors.toList());
    }
}