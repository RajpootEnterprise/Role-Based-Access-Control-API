package com.rbaciam.service;

import java.util.List;

import com.rbaciam.dto.IndustryResponse;

public interface IndustryService {
	 List<IndustryResponse> getAllIndustries(Long userId);
}
