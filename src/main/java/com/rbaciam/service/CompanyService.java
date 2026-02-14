package com.rbaciam.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import com.rbaciam.dto.CompanyDTO;
import com.rbaciam.dto.GetAllComapanyDTO;
import com.rbaciam.dto.PaginatedResponse;
import com.rbaciam.dto.UpdateCompanyDto;
import com.rbaciam.exception.AuthenticationExceptionFailed;
import com.rbaciam.exception.BadRequestException;
import com.rbaciam.exception.DuplicateException;
import com.rbaciam.exception.NotFoundException;
import com.rbaciam.exception.UnauthorizedException;

public interface CompanyService {

	Map<String, Object> createCompany(CompanyDTO companyDTO, Long userId)
			throws AuthenticationExceptionFailed, BadRequestException, DuplicateException, UnauthorizedException;

	Map<String, Object> updateCompany(Long id, UpdateCompanyDto updatecompanyDTO, Long userId)
			throws AuthenticationExceptionFailed, BadRequestException, NotFoundException, UnauthorizedException;

	PaginatedResponse<GetAllComapanyDTO> getCompaniesByUserId(Long userId, int page, int size);

	void deleteCompany(Long id, Long userId)
			throws AuthenticationExceptionFailed, BadRequestException, NotFoundException, UnauthorizedException;

	Optional<GetAllComapanyDTO> getCompanyById(Long id, Long userId) throws BadRequestException;

	 PaginatedResponse<CompanyDTO> filterCompanies(String country, String industry,
		        LocalDateTime createdFrom, LocalDateTime createdTo,
		        int page, int size, Long userId);

	PaginatedResponse<GetAllComapanyDTO> getAllCompaniesSearch(int page, int size, Long userId, String search);
}