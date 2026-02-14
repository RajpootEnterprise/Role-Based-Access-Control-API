package com.rbaciam.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rbaciam.dto.CountryResponse;
import com.rbaciam.dto.IndustryResponse;
import com.rbaciam.dto.TimezoneResponse;
import com.rbaciam.service.CountryService;
import com.rbaciam.service.IndustryService;
import com.rbaciam.service.TimezoneService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
	@RequestMapping("/api/utils")
public class CountryController {

	@Autowired
	private CountryService countryService;

	@Autowired
	private TimezoneService timezoneService;

	@Autowired
	private IndustryService industryService;

	@Operation(summary = "Get all timezones", description = "Returns a list of all available timezones")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved")
	@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid user ID")
	@ApiResponse(responseCode = "500", description = "Internal server error")
	@GetMapping("/get_alltimezones")
	public ResponseEntity<List<TimezoneResponse>> getAllTimezones(@RequestHeader("X-User-Id") Long userId) {
		List<TimezoneResponse> timezones = timezoneService.getAllTimezones(userId);
		return ResponseEntity.ok(timezones);
	}
    
	@Operation(summary = "Get all industries", description = "Returns a list of all available industries")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved")
	@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid user ID")
	@ApiResponse(responseCode = "500", description = "Internal server error")
	@GetMapping("/get_allindustries")
	public ResponseEntity<List<IndustryResponse>> getAllIndustries(@RequestHeader("X-User-Id") Long userId) {
		List<IndustryResponse> industries = industryService.getAllIndustries(userId);
		return ResponseEntity.ok(industries);
	}
    
	@Operation(summary = "Get all country", description = "Returns a list of all available country")
	@ApiResponse(responseCode = "200", description = "Successfully retrieved")
	@ApiResponse(responseCode = "401", description = "Unauthorized - Invalid user ID")
	@ApiResponse(responseCode = "500", description = "Internal server error")
	@GetMapping("/get_allcountry")
	public ResponseEntity<List<CountryResponse>> getAllCountries(@RequestHeader("X-User-Id") Long userId) {
		List<CountryResponse> countries = countryService.getAllCountries(userId);
		return ResponseEntity.ok(countries);
	}
}
