package com.rbaciam.service;

import java.util.List;

import com.rbaciam.dto.CountryResponse;

public interface CountryService {
	
	 List<CountryResponse> getAllCountries(Long userId);
	 

}
