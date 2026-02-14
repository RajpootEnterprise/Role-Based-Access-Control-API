package com.rbaciam.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rbaciam.dto.ApiResponseDto;
import com.rbaciam.dto.CompanyDTO;
import com.rbaciam.dto.GetAllComapanyDTO;
import com.rbaciam.dto.PaginatedResponse;
import com.rbaciam.dto.UpdateCompanyDto;
import com.rbaciam.service.CompanyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

	@Autowired
	private CompanyService companyService;

	private static final Logger logger = LoggerFactory.getLogger("TraceLogger");

	@PostMapping("/create_company")
	public ResponseEntity<ApiResponseDto> createCompany(@Valid @RequestBody CompanyDTO companyDTO,
			@RequestHeader("X-user-Id") Long userId) {
		Map<String, Object> createdData = companyService.createCompany(companyDTO, userId);
		Long createdId = (Long) createdData.get("id");
		CompanyDTO company = (CompanyDTO) createdData.get("company");

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponseDto(createdId, true, "Company created successfully"));
	}

	@Operation(summary = "Update a company", description = "Accessible by Admin for their company or Super Admin")
	@ApiResponse(responseCode = "200", description = "Company updated successfully")
	@ApiResponse(responseCode = "404", description = "Company not found")
	@PutMapping("/{company_id}")
	public ResponseEntity<ApiResponseDto> updateCompany(@PathVariable("company_id") Long id,
			@Valid @RequestBody UpdateCompanyDto updatecompanyDTO, @RequestHeader("X-User-Id") Long userId) {
		Map<String, Object> createdData = companyService.updateCompany(id, updatecompanyDTO, userId);
		Long createdId = (Long) createdData.get("id");
		CompanyDTO company = (CompanyDTO) createdData.get("company");
		return ResponseEntity.status(HttpStatus.OK)
				.body(new ApiResponseDto(createdId, true, "Company updated successfully"));
	}

	@Operation(summary = "Delete a company", description = "Accessible by Super Admin only")
	@ApiResponse(responseCode = "204", description = "Company deleted successfully")
	@ApiResponse(responseCode = "404", description = "Company not found")
	@DeleteMapping("/{company_id}")
	public ResponseEntity<ApiResponseDto> deleteCompany(@RequestParam("company_id") Long id,
			@RequestHeader("X-User-Id") Long userId) {
		companyService.deleteCompany(id, userId);
		return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto(true, "Company deleted successfully"));
	}

	@Operation(summary = "Get a company by ID")
	@ApiResponse(responseCode = "200", description = "Company retrieved successfully")
	@ApiResponse(responseCode = "404", description = "Company not found")
	@GetMapping("/{company_id}")
	public ResponseEntity<GetAllComapanyDTO> getCompanyById(@RequestHeader("X-User-Id") Long userId,
			@PathVariable("company_id") Long id) {
		return companyService.getCompanyById(id, userId).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@Operation(summary = "Get companies accessible by user", description = "Returns paginated companies for super-admin, or user's company for regular users")
	@ApiResponse(responseCode = "200", description = "Companies retrieved successfully")
	@ApiResponse(responseCode = "404", description = "Company not found")
	@GetMapping("/byuser")
	public ResponseEntity<PaginatedResponse<GetAllComapanyDTO>> getCompaniesByUserId(
			@RequestHeader("X-User-Id") Long userId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		PaginatedResponse<GetAllComapanyDTO> response = companyService.getCompaniesByUserId(userId, page, size);

		if (response.getData().isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(response);
	}

	@PostMapping("/filter")
	@Operation(summary = "companies search filter by fields", description = "Filter companies by country, industry, and created date range")
	@ApiResponse(responseCode = "200", description = "Filtered companies retrieved successfully")
	public ResponseEntity<PaginatedResponse<CompanyDTO>> filterCompanies(
	        @RequestHeader("X-User-Id") Long userId,
	        @RequestParam(required = false) String country,
	        @RequestParam(required = false) String industry,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {

	        return ResponseEntity.ok(companyService.filterCompanies(
	                country, industry, createdFrom, createdTo, page, size, userId));
	}



	@Operation(summary = "Get all companies Search", description = "Returns paginated list of companies")
	@ApiResponse(responseCode = "200", description = "Companies retrieved successfully")
	@GetMapping("/search_companies")
	public ResponseEntity<PaginatedResponse<GetAllComapanyDTO>> getAllCompaniesSearch(
			@RequestHeader("X-User-Id") Long userId, @RequestParam(name = "Search", required = false) String search,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		PaginatedResponse<GetAllComapanyDTO> response = companyService.getAllCompaniesSearch(page, size, userId,
				search);
		return ResponseEntity.ok(response);
	}

}