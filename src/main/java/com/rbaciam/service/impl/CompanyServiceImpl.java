package com.rbaciam.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rbaciam.dto.CompanyDTO;
import com.rbaciam.dto.GetAllComapanyDTO;
import com.rbaciam.dto.PaginatedResponse;
import com.rbaciam.dto.UpdateCompanyDto;
import com.rbaciam.entity.Company;
import com.rbaciam.entity.Permission;
import com.rbaciam.entity.Role;
import com.rbaciam.entity.RolePermission;
import com.rbaciam.entity.User;
import com.rbaciam.exception.AuthenticationExceptionFailed;
import com.rbaciam.exception.BadRequestException;
import com.rbaciam.exception.DuplicateException;
import com.rbaciam.exception.InternalServerException;
import com.rbaciam.exception.NotFoundException;
import com.rbaciam.exception.UnauthorizedException;
import com.rbaciam.repository.CompanyRepository;
import com.rbaciam.repository.UserRepository;
import com.rbaciam.service.CompanyService;
import com.rbaciam.service.PermissionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {
	private final CompanyRepository companyRepository;
	private final PermissionService permissionService;
	private final UserRepository userRepository;

	private static final Logger logger = LoggerFactory.getLogger("TraceLogger");

	@Override
	@Transactional
	public Map<String, Object> createCompany(CompanyDTO companyDTO, Long userId)
			throws AuthenticationExceptionFailed, BadRequestException, DuplicateException, UnauthorizedException {
		try {

			if (userId == null || userId <= 0) {
				throw new AuthenticationExceptionFailed("Invalid user ID: " + userId);
			}
			if (!permissionService.hasPermission(userId, "super_admin_access")) {
				throw new UnauthorizedException("User lacks permission to create company");
			}

			validateDomainFormat(companyDTO.getDomain());

			if (companyRepository.findByDomainAndDeletedAtIsNull(companyDTO.getDomain()).isPresent()) {
				throw new DuplicateException("Domain already exists: " + companyDTO.getDomain());
			}

			Company company = mapToEntity(companyDTO);
			logger.info("TRACE_LOG | Action=CREATE_COMPANY | Domain={} | Name={} | UserId={}", company.getDomain(),
					company.getName(), userId);

			company.setCreatedAt(LocalDateTime.now());
			company.setUpdatedAt(LocalDateTime.now());
			company.setCreatedBy(userId);
			company.setUpdatedBy(userId);

			Company savedCompany = companyRepository.save(company);
			CompanyDTO savedCompanyDTO = mapToDTO(savedCompany);

			Map<String, Object> response = new HashMap<>();
			response.put("id", company.getId());
			response.put("company", savedCompanyDTO);
			return response;

		} catch (DataAccessException ex) {
			throw new InternalServerException("Failed to create company due to database error");
		}
	}

	private void validateDomainFormat(String domain) throws BadRequestException {
		if (domain == null || domain.trim().isEmpty()) {
			throw new BadRequestException("Domain is mandatory");
		}

		String trimmedDomain = domain.trim();

		if (trimmedDomain.contains("@")) {
			if (!trimmedDomain.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
				throw new BadRequestException("Invalid email format");
			}
		}

		else {
			if (!trimmedDomain.matches("^[a-zA-Z0-9-]+\\.[a-zA-Z]{2,}$")) {
				throw new BadRequestException("Invalid domain format");
			}
		}
	}

	@Override
	@Transactional
	@CacheEvict(value = "companies", key = "#id")
	public Map<String, Object> updateCompany(Long id, UpdateCompanyDto updatedCompanyDTO, Long userId)
			throws AuthenticationExceptionFailed, BadRequestException, NotFoundException, UnauthorizedException {
		try {
			if (id == null || id <= 0) {
				throw new BadRequestException("Invalid company ID: " + id);
			}
			if (userId == null || userId <= 0) {
				throw new AuthenticationExceptionFailed("Invalid user ID: " + userId);
			}

			Company company = companyRepository.findByIdAndDeletedAtIsNull(id)
					.orElseThrow(() -> new NotFoundException("Company not found: " + id));

			User user = userRepository.findByIdAndDeletedAtIsNull(userId)
					.orElseThrow(() -> new NotFoundException("User not found: " + userId));

			if (!hasUpdatePermission(user, company)) {
				throw new UnauthorizedException("You don't have permission to update this company");
			}

			updateCompanyFields(company, updatedCompanyDTO);
			company.setUpdatedAt(LocalDateTime.now());
			company.setUpdatedBy(userId);
			logger.info("TRACE_LOG | Action=UPDATE_COMPANY | ID={} | Name={} | UserId={}", id, company.getName(),
					userId);

			Company savedCompany = companyRepository.save(company);
			CompanyDTO savedCompanyDTO = mapToDTO(savedCompany);

			Map<String, Object> response = new HashMap<>();
			response.put("id", company.getId());
			response.put("company", savedCompanyDTO);
			return response;

		} catch (DataAccessException ex) {
			throw new InternalServerException("Failed to update company due to database error");
		}

	}

	private void updateCompanyFields(Company company, UpdateCompanyDto dto) {

		if (dto.getName() != null && !dto.getName().isBlank()) {
			company.setName(dto.getName().trim());
		}

		if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
			company.setDescription(dto.getDescription());
		}

		if (dto.getCountry() != null && !dto.getCountry().isBlank()) {
			company.setCountry(dto.getCountry().trim());
		}

		if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
			company.setAddress(dto.getAddress().trim());
		}

		if (dto.getTimezone() != null && !dto.getTimezone().isBlank()) {
			company.setTimezone(dto.getTimezone().trim());
		}

		if (dto.getIndustry() != null && !dto.getIndustry().isBlank()) {
			company.setIndustry(dto.getIndustry().trim());
		}

		if (dto.getHomepage() != null && !dto.getHomepage().isBlank()) {
			company.setHomepage(dto.getHomepage());
		}
	}

	private UpdateCompanyDto convertToUpdateDto(Company company) {
		return UpdateCompanyDto.builder().name(company.getName()).description(company.getDescription())
				.country(company.getCountry()).address(company.getAddress()).timezone(company.getTimezone())
				.industry(company.getIndustry()).homepage(company.getHomepage()).build();
	}

	private boolean hasUpdatePermission(User user, Company company) {
		if (user == null || user.getRole() == null)
			return false;

		Set<String> permissions = Optional.ofNullable(user.getRole()).map(Role::getRolePermissions)
				.orElse(Collections.emptySet()).stream().filter(Objects::nonNull).map(RolePermission::getPermission)
				.filter(Objects::nonNull).map(Permission::getName).filter(Objects::nonNull).collect(Collectors.toSet());

		boolean isSuperAdmin = permissions.contains("super_admin_access");
		boolean isAdmin = permissions.contains("admin_access");

		if (isSuperAdmin) {
			return true;
		}
		return isAdmin && user.getCompany() != null && user.getCompany().getId().equals(company.getId());
	}

	@Override
	@Transactional
	@CacheEvict(value = "companies", key = "#id")
	public void deleteCompany(Long id, Long userId)
			throws AuthenticationExceptionFailed, BadRequestException, NotFoundException, UnauthorizedException {
		try {
			if (id == null || id <= 0) {
				throw new BadRequestException("Invalid company ID: " + id);
			}
			if (userId == null || userId <= 0) {
				throw new AuthenticationExceptionFailed("Invalid user ID: " + userId);
			}
			Company company = companyRepository.findByIdAndDeletedAtIsNull(id)
					.orElseThrow(() -> new NotFoundException("Company not found: " + id));

			if (!permissionService.hasPermission(userId, "super_admin_access")) {
				throw new UnauthorizedException("You don't have permission to delete this company");
			}

			company.setDeletedAt(LocalDateTime.now());
			company.setDeletedBy(userId);
			logger.info("TRACE_LOG | Action=DELETE_COMPANY | ID={} | UserId={}", id, userId);
			companyRepository.save(company);
		} catch (DataAccessException ex) {
			throw new InternalServerException("Failed to delete company due to database error");
		}
	}

	@Override
	@Cacheable(value = "companies", key = "{#userId, #page, #size, #isSuperAdmin}")
	public PaginatedResponse<GetAllComapanyDTO> getCompaniesByUserId(Long userId, int page, int size) {
		try {
			if (userId == null || userId <= 0) {
				throw new BadRequestException("Invalid userId: " + userId);
			}

			User user = userRepository.findByIdAndDeletedAtIsNull(userId)
					.orElseThrow(() -> new NotFoundException("User not found: " + userId));

			boolean isSuperAdmin = permissionService.hasPermission(userId, "super_admin_access");
			Pageable pageable = PageRequest.of(page, size);

			Page<Company> companyPage;
			if (isSuperAdmin) {

				companyPage = companyRepository.findAllByDeletedAtIsNull(pageable);
			} else {

				Company company = user.getCompany();
				if (company == null || company.getDeletedAt() != null) {
					throw new NotFoundException("Company not found for user: " + userId);
				}
				companyPage = new PageImpl<>(Collections.singletonList(company), pageable, 1);
			}

			logger.info("TRACE_LOG | Action=GET_COMPANIES | UserId={} | IsSuperAdmin={} | Page={} | Size={}", userId,
					isSuperAdmin, page, size);

			List<GetAllComapanyDTO> dtos = companyPage.getContent().stream().map(company -> {
				GetAllComapanyDTO dto = new GetAllComapanyDTO();
				dto.setId(company.getId());
				dto.setName(company.getName());
				dto.setDescription(company.getDescription());
				dto.setDomain(company.getDomain());
				dto.setCountry(company.getCountry());
				dto.setAddress(company.getAddress());
				dto.setTimezone(company.getTimezone());
				dto.setIndustry(company.getIndustry());
				dto.setHomepage(company.getHomepage());
				dto.setCreated_On(company.getCreatedAt());
				dto.setUserCount((int) company.getUsers().stream().filter(User -> user.getDeletedAt() == null).count());
				return dto;
			}).collect(Collectors.toList());

			return new PaginatedResponse<>(dtos, companyPage.getNumber(), companyPage.getSize(),
					(int) companyPage.getTotalElements(), companyPage.getTotalPages());

		} catch (DataAccessException ex) {
			logger.error("Database error while fetching companies", ex);
			throw new InternalServerException("Failed to fetch companies due to database error");
		}
	}

	@Override
	@Cacheable(value = "companies", key = "#id")
	public Optional<GetAllComapanyDTO> getCompanyById(Long id, Long userId)
			throws BadRequestException, NotFoundException, UnauthorizedException {
		try {

			if (id == null || id <= 0) {
				throw new BadRequestException("Invalid company ID: " + id);
			}

			Company company = companyRepository.findByIdAndDeletedAtIsNull(id)
					.orElseThrow(() -> new NotFoundException("Company not found: " + id));

			User user = userRepository.findByIdAndDeletedAtIsNull(userId)
					.orElseThrow(() -> new NotFoundException("User not found: " + userId));

			if (!hasViewPermission(user, company)) {
				throw new UnauthorizedException("You don't have permission to view this company");
			}

			logger.info("TRACE_LOG | Action=GET_COMPANY_BY_ID | ID={} | UserId={}", id, userId);
			logger.info("TRACE_LOG | Action=GET_COMPANY_BY_ID | ID={} | UserId={}", id, userId);

			GetAllComapanyDTO dto = new GetAllComapanyDTO();
			dto.setId(company.getId());
			dto.setName(company.getName());
			dto.setDescription(company.getDescription());
			dto.setDomain(company.getDomain());
			dto.setCountry(company.getCountry());
			dto.setAddress(company.getAddress());
			dto.setTimezone(company.getTimezone());
			dto.setIndustry(company.getIndustry());
			dto.setHomepage(company.getHomepage());
			dto.setCreated_On(company.getCreatedAt());
			dto.setUserCount((int) company.getUsers().stream().filter(u -> u.getDeletedAt() == null).count());

			return Optional.of(dto);

		} catch (DataAccessException ex) {
			logger.error("Database error while fetching company", ex);
			throw new InternalServerException("Failed to fetch company due to database error");
		}
	}

	private boolean hasViewPermission(User user, Company requestedCompany) {
		if (user == null || user.getRole() == null) {
			return false;
		}

		Set<String> permissions = ((Role) user.getRole()).getRolePermissions().stream().filter(Objects::nonNull)
				.map(RolePermission::getPermission).filter(Objects::nonNull).map(Permission::getName)
				.collect(Collectors.toSet());

		if (permissions.contains("super_admin_access")) {
			return true;
		}

		if (permissions.contains("admin_access")) {
			return user.getCompany() != null && user.getCompany().getId().equals(requestedCompany.getId());
		}

		if (permissions.contains("user_access")) {
			return user.getCompany() != null && user.getCompany().getId().equals(requestedCompany.getId());
		}

		return false;
	}

	private GetAllComapanyDTO mapToDTOAllCompany(Company company) {
		GetAllComapanyDTO dto = new GetAllComapanyDTO();
		dto.setId(company.getId());
		dto.setName(company.getName());
		dto.setDescription(company.getDescription());
		dto.setDomain(company.getDomain());
		dto.setCountry(company.getCountry());
		dto.setAddress(company.getAddress());
		dto.setTimezone(company.getTimezone());
		dto.setIndustry(company.getIndustry());
		dto.setHomepage(company.getHomepage());
		dto.setCreated_On(company.getCreatedAt());
		long userCount = company.getUsers().stream()
				.filter(user -> user.getDeletedAt() == null && !hasSuperAdminAccess(user)).count();
		dto.setUserCount((int) userCount);
		return dto;
	}

	private boolean hasSuperAdminAccess(User user) {
		if (user == null || user.getRole() == null) {
			return false;
		}

		return ((Role) user.getRole()).getRolePermissions().stream().filter(Objects::nonNull)
				.map(RolePermission::getPermission).filter(Objects::nonNull).map(Permission::getName)
				.anyMatch("super_admin_access"::equals);
	}

	@Override
	public PaginatedResponse<CompanyDTO> filterCompanies(String country, String industry,
	        LocalDateTime createdFrom, LocalDateTime createdTo,
	        int page, int size, Long userId) {

	    String requestId = UUID.randomUUID().toString();
	    MDC.put("requestId", requestId);
	    try {
	        logger.info("SERVICE_LOG | Filtering companies for userId={}, country={}, industry={}, from={}, to={}",
	                userId, country, industry, createdFrom, createdTo);

	        if (!permissionService.hasPermission(userId, "super_admin_access")) {
	            throw new UnauthorizedException("User lacks permission to filter companies");
	        }

	       
	        page = Math.max(page, 0);
	        size = size <= 0 ? 10 : Math.min(size, 100);
	        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

	       
	        String normalizedCountry = StringUtils.isBlank(country) ? null : country.trim().toLowerCase();
	        String normalizedIndustry = StringUtils.isBlank(industry) ? null : industry.trim().toLowerCase();

	        Page<Company> companyPage = companyRepository.findByFilters(
	                normalizedCountry, normalizedIndustry, createdFrom, createdTo, pageable);

	        List<CompanyDTO> dtos = companyPage.getContent().stream()
	                .map(this::mapToDTOFilter)
	                .filter(Objects::nonNull)
	                .collect(Collectors.toList());

	        return new PaginatedResponse<>(dtos, companyPage.getNumber(), companyPage.getSize(),
	                (int) companyPage.getTotalElements(), companyPage.getTotalPages());

	    } catch (UnauthorizedException | BadRequestException ex) {
	        throw ex;
	    } catch (Exception ex) {
	        logger.error("SERVICE_LOG | Unexpected error while filtering companies", ex);
	        throw new InternalServerException("Unexpected error occurred while filtering companies");
	    } finally {
	        MDC.clear();
	    }
	}



	private CompanyDTO mapToDTOFilter(Company company) {

		CompanyDTO dto = new CompanyDTO();
		dto.setName(company.getName());
		dto.setCountry(company.getCountry());
		dto.setDescription(company.getDescription());
		dto.setDomain(company.getDomain());
		dto.setCountry(company.getCountry());
		dto.setAddress(company.getAddress());
		dto.setTimezone(company.getTimezone());
		dto.setIndustry(company.getIndustry());
		dto.setHomepage(company.getHomepage());
		dto.setUserCount((int) company.getUsers().stream().filter(u -> u.getDeletedAt() == null).count());
		return dto;
	}

	private void validateCompanyDTO(CompanyDTO companyDTO) {
		if (companyDTO == null) {
			throw new BadRequestException("Company DTO cannot be null");
		}
		if (companyDTO.getName() == null || companyDTO.getName().trim().isEmpty()) {
			throw new BadRequestException("Company name is required");
		}
		if (companyDTO.getDomain() == null || !companyDTO.getDomain().matches("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
			throw new BadRequestException("Invalid domain format");
		}
	}

	private Company mapToEntity(CompanyDTO companyDTO) {
		Company company = new Company();
		company.setName(companyDTO.getName());
		company.setDescription(companyDTO.getDescription());
		company.setDomain(companyDTO.getDomain());
		company.setCountry(companyDTO.getCountry());
		company.setAddress(companyDTO.getAddress());
		company.setTimezone(companyDTO.getTimezone());
		company.setIndustry(companyDTO.getIndustry());
		company.setHomepage(companyDTO.getHomepage());
		return company;
	}

	private CompanyDTO mapToDTO(Company company) {
		CompanyDTO companyDTO = new CompanyDTO();
		companyDTO.setName(company.getName());
		companyDTO.setDescription(company.getDescription());
		companyDTO.setDomain(company.getDomain());
		companyDTO.setCountry(company.getCountry());
		companyDTO.setAddress(company.getAddress());
		companyDTO.setTimezone(company.getTimezone());
		companyDTO.setIndustry(company.getIndustry());
		companyDTO.setHomepage(company.getHomepage());
		companyDTO.setCreated_On(company.getCreatedAt());
		return companyDTO;
	}

	@Override
	public PaginatedResponse<GetAllComapanyDTO> getAllCompaniesSearch(int page, int size, Long userId, String search) {

		try {
			logger.info("TRACE_LOG | Action=GET_ALL_COMPANIES_SEARCH | UserId={} | Search='{}'", userId, search);

			if (page < 0 || size <= 0) {
				throw new BadRequestException("Invalid pagination parameters");
			}

			User actingUser = userRepository.findByIdAndDeletedAtIsNull(userId)
					.orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

			List<Company> filteredCompanies;

			if (hasSuperAdminAccess(actingUser)) {

				filteredCompanies = companyRepository.findAllByDeletedAtIsNull(Sort.by("name").ascending()).stream()
						.filter(company -> {
							List<User> activeUsers = company.getUsers().stream()
									.filter(user -> user.getDeletedAt() == null).collect(Collectors.toList());

							boolean hasNonSuperAdmin = activeUsers.stream()
									.anyMatch(user -> !hasSuperAdminAccess(user));

							return hasNonSuperAdmin || activeUsers.isEmpty();
						}).collect(Collectors.toList());

			} else {
				Company userCompany = actingUser.getCompany();
				if (userCompany == null || userCompany.getDeletedAt() != null) {
					throw new NotFoundException("User has no valid company.");
				}

				if (!hasViewPermission(actingUser, userCompany)) {
					throw new UnauthorizedException("You don't have permission to view this company.");
				}

				filteredCompanies = List.of(userCompany);
			}

			if (search != null && !search.isBlank()) {
				String lowerSearch = search.toLowerCase();
				filteredCompanies = filteredCompanies.stream().filter(company -> (company.getName() != null
						&& company.getName().toLowerCase().contains(lowerSearch))
						|| (company.getDomain() != null && company.getDomain().toLowerCase().contains(lowerSearch)))
						.collect(Collectors.toList());

				logger.debug("Filtered companies count after search='{}': {}", search, filteredCompanies.size());
			}

			int total = filteredCompanies.size();
			int start = Math.min(page * size, total);
			int end = Math.min(start + size, total);
			List<GetAllComapanyDTO> dtoPage = total > 0 ? filteredCompanies.subList(start, end).stream()
					.map(this::mapToDTOAllCompany).collect(Collectors.toList()) : Collections.emptyList();

			return new PaginatedResponse<>(dtoPage, page, size, total, (int) Math.ceil((double) total / size));

		} catch (UnauthorizedException | NotFoundException | BadRequestException ex) {
			throw ex;
		} catch (DataAccessException ex) {
			logger.error("Database error while searching companies", ex);
			throw new InternalServerException("Failed to fetch companies due to database error");
		} catch (IndexOutOfBoundsException ex) {
			logger.error("Pagination error while searching companies", ex);
			throw new BadRequestException("Invalid pagination parameters");
		} catch (Exception ex) {
			logger.error("Unexpected error during getAllCompaniesSearch", ex);
			throw new InternalServerException("Unexpected error occurred");
		}
	}

}
