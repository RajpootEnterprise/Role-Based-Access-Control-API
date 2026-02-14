package com.rbaciam.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rbaciam.dto.CreateRoleDTO;
import com.rbaciam.dto.PaginatedResponse;
import com.rbaciam.dto.PermissionDTO;
import com.rbaciam.dto.RoleDTO;
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
import com.rbaciam.repository.RoleRepository;
import com.rbaciam.repository.UserRepository;
import com.rbaciam.service.PermissionService;
import com.rbaciam.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
	private final RoleRepository roleRepository;
	private final PermissionService permissionService;
	private final UserRepository userRepository;
	private static final Logger logger = LoggerFactory.getLogger("TraceLogger");

	@Override
	@Transactional
	@CacheEvict(value = "roles", allEntries = true)
	public CreateRoleDTO createRole(CreateRoleDTO roleDTO, Long userId) {
		String requestId = UUID.randomUUID().toString();
		MDC.put("requestId", requestId);
		MDC.put("userId", String.valueOf(userId));

		try {

			validateUserAndInput(userId, roleDTO);

			validateRoleCreationPermissionsForCreate(roleDTO, userId);

			checkForDuplicateRoleName(roleDTO.getName());

			Role role = mapAndPrepareRole(roleDTO, userId);
			Role savedRole = roleRepository.save(role);

			logger.info("SERVICE_LOG | Role created successfully: {}", role.getName());
			return mapToDTOCreate(savedRole);

		} catch (DataAccessException ex) {
			logger.error("SERVICE_LOG | Database error while creating role: {}", ex.getMessage());
			throw new InternalServerException("Failed to create role due to database error");
		} finally {
			MDC.clear();
		}
	}

	private void validateUserAndInput(Long userId, CreateRoleDTO roleDTO) {
		if (userId == null || userId <= 0) {
			logger.error("SERVICE_LOG | Invalid user ID for role creation");
			throw new AuthenticationExceptionFailed("Invalid user ID: " + userId);
		}
		if (roleDTO == null) {
			logger.error("SERVICE_LOG | Role DTO cannot be null");
			throw new BadRequestException("Role data cannot be empty");
		}
	}

	private void validateRoleCreationPermissions(RoleDTO roleDTO, Long userId) {
		Role.Type roleType = Role.Type.valueOf(roleDTO.getType().toUpperCase());

		if (roleType == Role.Type.DEFAULT) {
			if (!permissionService.hasPermission(userId, "super_admin_access")) {
				logger.warn("SERVICE_LOG | User ID={} attempted to create DEFAULT role without permission", userId);
				throw new UnauthorizedException("Only Super Admin can create default roles");
			}
		} else {
			if (!permissionService.hasPermission(userId, "create_role")) {
				logger.warn("SERVICE_LOG | User ID={} lacks CREATE_ROLE permission", userId);
				throw new UnauthorizedException("User lacks permission to create roles");
			}
		}
	}

	private void validateRoleCreationPermissionsForCreate(CreateRoleDTO roleDTO, Long userId) {
		Role.Type roleType = Role.Type.valueOf(roleDTO.getType().toUpperCase());

		if (roleType == Role.Type.DEFAULT) {
			if (!permissionService.hasPermission(userId, "super_admin_access")) {
				logger.warn("SERVICE_LOG | User ID={} attempted to create DEFAULT role without permission", userId);
				throw new UnauthorizedException("Only Super Admin can create default roles");
			}
		} else {
			if (!permissionService.hasPermission(userId, "create_role")) {
				logger.warn("SERVICE_LOG | User ID={} lacks CREATE_ROLE permission", userId);
				throw new UnauthorizedException("User lacks permission to create roles");
			}
		}
	}

	private void checkForDuplicateRoleName(String roleName) {
		if (roleRepository.findByNameAndDeletedAtIsNull(roleName).isPresent()) {
			logger.error("SERVICE_LOG | Duplicate role name: {}", roleName);
			throw new DuplicateException("Role name already exists: " + roleName);
		}
	}

	private Role mapAndPrepareRole(CreateRoleDTO roleDTO, Long userId) {
		Role role = new Role();
		role.setName(roleDTO.getName());
		role.setType(Role.Type.valueOf(roleDTO.getType().toUpperCase()));
		role.setCreatedAt(LocalDateTime.now());
		role.setUpdatedAt(LocalDateTime.now());
		role.setCreatedBy(userId);
		role.setUpdatedBy(userId);
		logger.info("TRACE_LOG | Action=CREATE_ROLE | Name={} | Type={} | UserId={}", role.getName(), role.getType(),
				userId);

		return role;
	}

	@Override
	@Transactional
	@CacheEvict(value = "roles", key = "#id")
	public CreateRoleDTO updateRole(Long id, CreateRoleDTO updatedRoleDTO, Long userId) {
		String requestId = UUID.randomUUID().toString();
		MDC.put("requestId", requestId);
		MDC.put("userId", String.valueOf(userId));
		try {
			if (id == null || id <= 0) {
				logger.error("SERVICE_LOG | Invalid role ID for update");
				throw new BadRequestException("Invalid role ID: " + id);
			}
			if (userId == null || userId <= 0) {
				logger.error("SERVICE_LOG | Invalid user ID for role update");
				throw new AuthenticationExceptionFailed("Invalid user ID: " + userId);
			}
			if (!permissionService.hasPermission(userId, "super_admin_access")) {
				logger.warn("SERVICE_LOG | User ID={} lacks UPDATE_ROLE permission", userId);
				throw new UnauthorizedException("User lacks permission to update role");
			}
			validateRoleDTO(updatedRoleDTO);
			Role role = roleRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> {
				logger.error("SERVICE_LOG | Role not found: {}", id);
				return new NotFoundException("Role not found: " + id);
			});
			if (role.getType() == Role.Type.DEFAULT && !updatedRoleDTO.getType().equals("DEFAULT")) {
				logger.error("SERVICE_LOG | Cannot modify DEFAULT role type for role ID={}", id);
				throw new BadRequestException("Cannot modify DEFAULT role type");
			}
			updateRoleFields(role, updatedRoleDTO);
			role.setUpdatedAt(LocalDateTime.now());
			role.setUpdatedBy(userId);
			logger.info("SERVICE_LOG | Updating role ID={} to Name={}", id, updatedRoleDTO.getName());
			logger.info("TRACE_LOG | Action=UPDATE_ROLE | ID={} | Name={} | UserId={}", id, role.getName(), userId);
			try {
				Role updatedRole = roleRepository.save(role);
				return mapToDTOCreate(updatedRole);
			} catch (DataAccessException ex) {
				logger.error("SERVICE_LOG | Database error while updating role: {}", ex.getMessage());
				throw new InternalServerException("Failed to update role due to database error");
			}
		} catch (Exception ex) {
			logger.error("SERVICE_LOG | Unexpected error while updating role: {}", ex.getMessage());
			throw new InternalServerException("Unexpected error occurred while updating role");
		} finally {
			MDC.clear();
		}
	}

	@Override
	@Transactional
	@CacheEvict(value = "roles", key = "#id")
	public void deleteRole(Long id, Long userId) {
		String requestId = UUID.randomUUID().toString();
		MDC.put("requestId", requestId);
		MDC.put("userId", String.valueOf(userId));
		try {
			if (id == null || id <= 0) {
				logger.error("SERVICE_LOG | Invalid role ID for deletion");
				throw new BadRequestException("Invalid role ID: " + id);
			}
			if (userId == null || userId <= 0) {
				logger.error("SERVICE_LOG | Invalid user ID for role deletion");
				throw new AuthenticationExceptionFailed("Invalid user ID: " + userId);
			}
			if (!permissionService.hasPermission(userId, "super_admin_access")) {
				logger.warn("SERVICE_LOG | User ID={} lacks DELETE_ROLE permission", userId);
				throw new UnauthorizedException("User lacks permission to delete role");
			}
			Role role = roleRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> {
				System.out.println("Role not found: " + id);
				return new NotFoundException("Role not found: " + id);
			});

			System.out.println("Role found: " + role.getName());
			System.out.println("Role type: " + role.getType());

			System.out.println("Setting deletedAt and deletedBy");
			role.setDeletedAt(LocalDateTime.now());
			role.setDeletedBy(userId);

			System.out.println("Saving role...");
			roleRepository.save(role);
			System.out.println("Role soft-deleted successfully.");

		} catch (DataAccessException ex) {
			logger.error("SERVICE_LOG | Database error while deleting role: {}", ex.getMessage());
			throw new InternalServerException("Failed to delete role due to database error");
		} catch (Exception ex) {
			logger.error("SERVICE_LOG | Unexpected error while deleting role: {}", ex.getMessage());
			throw new InternalServerException("Unexpected error occurred while deleting role");
		} finally {
			MDC.clear();
		}
	}

	@Override
	@Cacheable(value = "roles", key = "#id")
	public Optional<RoleDTO> getRoleById(Long userId, Long roleId) {
		validateUserAccess(userId);
		String requestId = UUID.randomUUID().toString();
		MDC.put("requestId", requestId);
		try {
			if (roleId == null || roleId <= 0) {
				logger.error("SERVICE_LOG | Invalid role ID for fetch");
				throw new BadRequestException("Invalid role ID: " + roleId);
			}
			logger.info("SERVICE_LOG | Fetching role by ID={}", roleId);
			logger.info("TRACE_LOG | Action=GET_ROLE_BY_ID | ID={}", roleId);
			try {
				return roleRepository.findByIdAndDeletedAtIsNull(roleId).map(this::mapToDTO);
			} catch (DataAccessException ex) {
				logger.error("SERVICE_LOG | Database error while fetching role: {}", ex.getMessage());
				throw new InternalServerException("Failed to fetch role due to database error");
			}
		} catch (Exception ex) {
			logger.error("SERVICE_LOG | Unexpected error while fetching role: {}", ex.getMessage());
			throw new InternalServerException("Unexpected error occurred while fetching role");
		} finally {
			MDC.clear();
		}
	}

	private void validateUserAccess(Long userId) {
		Optional<User> userOpt = userRepository.findByIdAndDeletedAtIsNull(userId);
		if (userOpt.isEmpty()) {
			throw new NotFoundException("User not found or inactive");
		}

		User user = userOpt.get();
		if (user.getRole() == null) {
			throw new UnauthorizedException("User does not have permission to access this resource");
		}

		Set<String> permissions = Optional.ofNullable(user.getRole().getRolePermissions())
				.orElse(Collections.emptySet()).stream().filter(Objects::nonNull).map(RolePermission::getPermission)
				.filter(Objects::nonNull).map(Permission::getName).filter(Objects::nonNull).collect(Collectors.toSet());

		boolean hasAccess = permissions.contains("super_admin_access") || permissions.contains("admin_access");

		if (!hasAccess) {
			throw new UnauthorizedException("User does not have permission to access this resource");
		}
	}

	@Override
	public PaginatedResponse<RoleDTO> filterRoles(String name, Long permissionId, LocalDateTime createdFrom,
			LocalDateTime createdTo, int page, int size, Long userId) {
		String requestId = UUID.randomUUID().toString();
		MDC.put("requestId", requestId);
		try {
			logger.info("SERVICE_LOG | Filtering roles | name={}, page={}, size={}, userId={}", name, page, size,
					userId);

			if (!permissionService.hasPermission(userId, "super_admin_access")
					&& !permissionService.hasPermission(userId, "admin_access")) {
				throw new UnauthorizedException("User lacks permission to filter roles");
			}

			page = Math.max(0, page);
			size = size <= 0 ? 10 : Math.min(size, 100);
			Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

			String normalizedName = StringUtils.isBlank(name) ? null : name.trim().toLowerCase();

			Page<Role> rolePage;
			try {
				rolePage = roleRepository.filterRolesByCriteria(normalizedName, permissionId, createdFrom, createdTo,
						pageable);
			} catch (DataAccessException ex) {
				logger.error("SERVICE_LOG | DB error while filtering roles: {}", ex.getMessage());
				throw new InternalServerException("Failed to filter roles due to DB error");
			}

			List<RoleDTO> dtoList = rolePage.getContent().stream().map(this::mapToDTO).filter(Objects::nonNull)
					.collect(Collectors.toList());

			return new PaginatedResponse<>(dtoList, rolePage.getNumber(), rolePage.getSize(),
					(int) rolePage.getTotalElements(), rolePage.getTotalPages());

		} catch (UnauthorizedException | BadRequestException ex) {
			throw ex;
		} catch (Exception ex) {
			logger.error("SERVICE_LOG | Unexpected error while filtering roles", ex);
			throw new InternalServerException("Unexpected error occurred while filtering roles");
		} finally {
			MDC.clear();
		}
	}

	public PaginatedResponse<RoleDTO> getAllRoles(Long userId, int page, int size, String search) {
		validateUserAccess(userId);

		try {
			if (page < 0 || size <= 0) {
				throw new BadRequestException("Page or size parameters are invalid");
			}

			User user = userRepository.findByIdAndDeletedAtIsNull(userId)
					.orElseThrow(() -> new NotFoundException("User not found: " + userId));

			Company company = user.getCompany();
			if (company == null || company.getDeletedAt() != null) {
				throw new NotFoundException("Company not found for user: " + userId);
			}

			if (!hasgetRolePermission(user, company)) {
				throw new UnauthorizedException("You don't have permission to get the roles");
			}

			Pageable pageable = PageRequest.of(page, size);
			Page<Role> rolePage;

			if (search != null && !search.isBlank()) {
				rolePage = roleRepository.findByDeletedAtIsNullAndNameNotAndNameContainingIgnoreCase("SUPER_ADMIN",
						search.trim(), pageable);
			} else {
				rolePage = roleRepository.findByDeletedAtIsNullAndNameNot("SUPER_ADMIN", pageable);
			}

			List<RoleDTO> roleDTOs = rolePage.getContent().stream().map(this::mapToDTO).collect(Collectors.toList());

			return new PaginatedResponse<>(roleDTOs, rolePage.getNumber(), rolePage.getSize(),
					(int) rolePage.getTotalElements(), rolePage.getTotalPages());

		} catch (Exception ex) {
			logger.error("SERVICE_LOG | Error fetching roles: {}", ex.getMessage(), ex);
			throw new InternalServerException("Failed to fetch roles");
		} finally {
			MDC.clear();
		}
	}

	private void validateRoleDTO(CreateRoleDTO roleDTO) {
		if (roleDTO == null) {
			logger.error("SERVICE_LOG | Role DTO is null");
			throw new BadRequestException("Role DTO cannot be null");
		}
		if (roleDTO.getName() == null || roleDTO.getName().trim().isEmpty()) {
			logger.error("SERVICE_LOG | Role name is missing");
			throw new BadRequestException("Role name is required");
		}
		if (!roleDTO.getType().equals("DEFAULT") && !roleDTO.getType().equals("MANUAL")) {
			logger.error("SERVICE_LOG | Invalid role type: {}", roleDTO.getType());
			throw new BadRequestException("Invalid role type");
		}
	}

	private Role mapToEntity(RoleDTO roleDTO) {
		Role role = new Role();
		role.setName(roleDTO.getName());
		role.setType(Role.Type.valueOf(roleDTO.getType()));
		return role;
	}

	private CreateRoleDTO mapToDTOCreate(Role role) {
		CreateRoleDTO dto = new CreateRoleDTO();
		dto.setName(role.getName());
		dto.setType(role.getType().name());
		return dto;

	}

	private RoleDTO mapToDTO(Role role) {
		RoleDTO dto = new RoleDTO();
		dto.setId(role.getId());
		dto.setName(role.getName());
		dto.setType(role.getType().name());

		List<PermissionDTO> permissionDTOs = role.getRolePermissions().stream().map(rp -> {
			Permission p = rp.getPermission();
			if (p == null)
				return null;
			PermissionDTO pdto = new PermissionDTO();
			pdto.setId(p.getId());
			pdto.setName(p.getName());
			return pdto;
		}).filter(Objects::nonNull).collect(Collectors.toList());

		dto.setPermissions(permissionDTOs);
		return dto;
	}

	private void updateRoleFields(Role role, CreateRoleDTO roleDTO) {
		role.setName(roleDTO.getName());
		role.setType(Role.Type.valueOf(roleDTO.getType()));
	}

	private boolean hasgetRolePermission(User user, Company company) {
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

	private boolean hasSuperAdminAccess(User user) {
		if (user == null || user.getRole() == null) {
			return false;
		}

		return user.getRole().getRolePermissions().stream().filter(Objects::nonNull).map(RolePermission::getPermission)
				.filter(Objects::nonNull).map(Permission::getName).anyMatch("super_admin_access"::equals);
	}
}