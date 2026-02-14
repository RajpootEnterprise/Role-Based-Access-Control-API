package com.rbaciam.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rbaciam.dto.IndustryResponse;
import com.rbaciam.entity.User;
import com.rbaciam.exception.NotFoundException;
import com.rbaciam.repository.IndustryRepository;
import com.rbaciam.repository.UserRepository;
import com.rbaciam.service.IndustryService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class IndustryServiceImpl implements IndustryService {

    @Autowired
    private IndustryRepository industryRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<IndustryResponse> getAllIndustries(Long userId) {
    	  User user = userRepository.findByIdAndDeletedAtIsNull(userId)
	                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
        return industryRepository.findAll()
                .stream()
                .map(industry -> new IndustryResponse(industry.getName()))
                .collect(Collectors.toList());
    }
}