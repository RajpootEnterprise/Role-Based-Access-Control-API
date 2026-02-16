package com.rbaciam.service.impl;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rbaciam.dto.PaginatedResponse;
import com.rbaciam.dto.UserCreateDTO;
import com.rbaciam.dto.UserDTO;
import com.rbaciam.dto.UserUpdateDTO;
import com.rbaciam.entity.Company;
import com.rbaciam.entity.Permission;
import com.rbaciam.entity.Role;
import com.rbaciam.entity.RolePermission;
import com.rbaciam.entity.User;
import com.rbaciam.exception.BadRequestException;
import com.rbaciam.exception.DuplicateException;
import com.rbaciam.exception.EmailSendingException;
import com.rbaciam.exception.InternalServerException;
import com.rbaciam.exception.NotFoundException;
import com.rbaciam.exception.UnauthorizedException;
import com.rbaciam.repository.CompanyRepository;
import com.rbaciam.repository.RoleRepository;
import com.rbaciam.repository.UserRepository;
import com.rbaciam.service.PermissionService;
import com.rbaciam.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final PermissionService permissionService;
	private final EmailService emailService;
	private final CompanyRepository companyRepository;

	@Value("${app.base-url}")
	private String baseUrl;

	private static final Logger logger = LoggerFactory.getLogger("TraceLogger");

	@Override
	public Map<String, Object> createUser(UserCreateDTO userCreateDTO, Long userId) {
		try {
			logger.info("Attempting to create user with email: {}", userCreateDTO.getEmail());

			if (userRepository.findByEmailAndDeletedAtIsNull(userCreateDTO.getEmail()).isPresent()) {
				logger.error("Duplicate email: {}", userCreateDTO.getEmail());
				throw new DuplicateException("Email already exists: " + userCreateDTO.getEmail());
			}

			User requestingUser = userRepository.findByIdAndDeletedAtIsNull(userId)
					.orElseThrow(() -> new NotFoundException("Requesting user not found: " + userId));

			Role role = roleRepository.findById(userCreateDTO.getRoleId())
					.orElseThrow(() -> new NotFoundException("Role not found"));

			Company company = companyRepository.findById(userCreateDTO.getCompanyId()).orElseThrow(
					() -> new NotFoundException("Company not found with ID: " + userCreateDTO.getCompanyId()));

			if (!hasPermission(requestingUser, company)) {
				throw new UnauthorizedException("You don't have permission to create users.");
			}

			String authToken = UUID.randomUUID().toString();

			User user = new User();
			user.setName(userCreateDTO.getName());
			user.setEmail(userCreateDTO.getEmail());
			user.setRole(role);
			user.setCompany(company);
			user.setStatus(User.Status.VPENDING);
			user.setAuthToken(authToken);
			user.setPassword(null);
			user.setCreatedAt(LocalDateTime.now());
			user.setUpdatedAt(LocalDateTime.now());
			user.setCreatedBy(userId);
			user.setUpdatedBy(userId);
			user.setPasswordChanged(false);

			userRepository.save(user);

			try {
				String validationUrl = baseUrl + "/validate/" + authToken;
				emailService.sendUserValidationLink(user.getEmail(), user.getName(), validationUrl);
			} catch (EmailSendingException e) {
				logger.error("Failed to send validation email, but user was created", e);
			}

			Map<String, Object> response = new HashMap<>();
			response.put("id", user.getId());
			response.put("user", user);

			return response;

		} catch (DuplicateException | NotFoundException | UnauthorizedException ex) {
			logger.error("Validation error creating user: {}", ex.getMessage());
			throw ex;
		} catch (Exception ex) {
			logger.error("Unexpected error creating user: {}", ex.getMessage());
			throw new InternalServerException("Unexpected error creating user");
		}
	}

	private String generateRandomPassword() {
		String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		String lower = "abcdefghijklmnopqrstuvwxyz";
		String digits = "0123456789";
		String special = "!@#$%^&*()";
		String all = upper + lower + digits + special;

		SecureRandom random = new SecureRandom();
		StringBuilder sb = new StringBuilder(12);

		sb.append(upper.charAt(random.nextInt(upper.length())));
		sb.append(lower.charAt(random.nextInt(lower.length())));
		sb.append(digits.charAt(random.nextInt(digits.length())));
		sb.append(special.charAt(random.nextInt(special.length())));

		for (int i = 4; i < 12; i++) {
			sb.append(all.charAt(random.nextInt(all.length())));
		}

		String result = sb.toString();
		char[] array = result.toCharArray();
		for (int i = array.length - 1; i > 0; i--) {
			int index = random.nextInt(i + 1);
			char temp = array[index];
			array[index] = array[i];
			array[i] = temp;
		}

		return new String(array);
	}

	@Override
	public Map<String, Object> updateUser(Long id, UserUpdateDTO dto, Long requesterId) {
		try {
			if (id == null || id <= 0 || requesterId == null || requesterId <= 0) {
				throw new BadRequestException("Invalid ID provided");
			}

			User targetUser = userRepository.findByIdAndDeletedAtIsNull(id)
					.orElseThrow(() -> new NotFoundException("User not found"));

			User actingUser = userRepository.findByIdAndDeletedAtIsNull(requesterId)
					.orElseThrow(() -> new NotFoundException("Requester not found"));

			if (!hasPermission(actingUser, targetUser.getCompany())) {
				throw new UnauthorizedException("No update permission");
			}

			if (dto.getName() != null) {
				targetUser.setName(dto.getName().trim());
			}

			if (dto.getRoleId() != null) {
				Role role = roleRepository.findById(dto.getRoleId())
						.orElseThrow(() -> new NotFoundException("Role not found"));
				targetUser.setRole(role);
			}

			if (dto.getStatus() != null) {
				if (!dto.getStatus().trim().isEmpty()) {
					try {
						targetUser.setStatus(User.Status.valueOf(dto.getStatus().trim().toUpperCase()));
					} catch (IllegalArgumentException e) {
						throw new BadRequestException(
								"Invalid status value. Valid values: " + Arrays.toString(User.Status.values()));
					}
				}
			}

			targetUser.setUpdatedAt(LocalDateTime.now());
			targetUser.setUpdatedBy(requesterId);
			userRepository.save(targetUser);

			Map<String, Object> response = new HashMap<>();
			response.put("id", targetUser.getId());
			response.put("user", targetUser);
			return response;

		} catch (BadRequestException | NotFoundException | UnauthorizedException e) {
			throw e;
		} catch (Exception e) {
			throw new InternalServerException("Update failed: " + e.getMessage());
		}
	}

	private boolean hasPermission(User actingUser, Company targetCompany) {
		if (actingUser == null || actingUser.getRole() == null)
			return false;

		Set<String> permissions = Optional.ofNullable(actingUser.getRole()).map(Role::getRolePermissions)
				.orElse(Collections.emptySet()).stream().filter(Objects::nonNull).map(RolePermission::getPermission)
				.filter(Objects::nonNull).map(Permission::getName).filter(Objects::nonNull).collect(Collectors.toSet());

		boolean isSuperAdmin = permissions.contains("super_admin_access");
		boolean isAdmin = permissions.contains("admin_access");

		if (isSuperAdmin)
			return true;

		return isAdmin && actingUser.getCompany() != null
				&& actingUser.getCompany().getId().equals(targetCompany.getId());
	}

	@Override
	public void deleteUser(Long id, Long requesterId) {

		User targetUser = userRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new NotFoundException("User not found: " + id));

		User actingUser = userRepository.findByIdAndDeletedAtIsNull(requesterId)
				.orElseThrow(() -> new NotFoundException("Requester user not found: " + requesterId));

		Company company = targetUser.getCompany();
		if (company == null || company.getDeletedAt() != null) {
			throw new NotFoundException("Target user has no valid company.");
		}

		if (!hasPermission(actingUser, company)) {
			throw new UnauthorizedException("You don't have permission to delete this user.");
		}

		targetUser.setDeletedAt(LocalDateTime.now());
		targetUser.setDeletedBy(requesterId);

		userRepository.save(targetUser);
	}

	@Override
	public Optional<UserDTO> getUserById(Long id, Long userId) {
		try {
			if (id == null || id <= 0) {
				logger.error("SERVICE_LOG | Invalid user ID for fetch");
				throw new BadRequestException("Invalid user ID: " + id);
			}

			logger.info("SERVICE_LOG | Fetching user by ID={}", id);
			logger.info("TRACE_LOG | Action=GET_USER_BY_ID | ID={}", id);

			User targetUser = userRepository.findByIdAndDeletedAtIsNull(id)
					.orElseThrow(() -> new NotFoundException("User not found: " + id));

			User actingUser = userRepository.findByIdAndDeletedAtIsNull(userId)
					.orElseThrow(() -> new NotFoundException("Requester user not found: " + userId));

			if (!hasSuperAdminAccess(actingUser)) {
				Company company = targetUser.getCompany();
				if (company == null || company.getDeletedAt() != null) {
					throw new NotFoundException("Target user has no valid company.");
				}

				if (!hasViewPermission(actingUser, company)) {
					throw new UnauthorizedException("You don't have permission to view this user");
				}
			}

			return Optional.of(mapToDTO(targetUser));

		} catch (BadRequestException ex) {
			logger.error("SERVICE_LOG | Bad request fetching user: {}", ex.getMessage());
			throw ex;
		} catch (Exception ex) {
			logger.error("SERVICE_LOG | Unexpected error fetching user: {}", ex.getMessage());
			throw new InternalServerException("Unexpected error fetching user: " + ex.getMessage());
		} finally {
			MDC.clear();
		}
	}

	private boolean hasViewPermission(User user, Company requestedCompany) {
		if (user == null || user.getRole() == null) {
			return false;
		}

		Set<String> permissions = user.getRole().getRolePermissions().stream().filter(Objects::nonNull)
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

	private UserDTO mapToDTO(User user) {
		UserDTO userDTO = new UserDTO();

		userDTO.setUserId(user.getId());
		userDTO.setName(user.getName());
		userDTO.setEmail(user.getEmail());
		userDTO.setRole_Id(user.getRole() != null ? user.getRole().getId() : null);
		userDTO.setCompanyId(user.getCompany() != null ? user.getCompany().getId() : null);
		userDTO.setStatus(user.getStatus().name());
		userDTO.setCompanyName(user.getCompany().getName());
		userDTO.setRoleName(user.getRole().getName());
		userDTO.setCreated_On(user.getCreatedAt());
		return userDTO;
	}

	private boolean hasSuperAdminAccess(User user) {
		if (user == null || user.getRole() == null) {
			return false;
		}

		return user.getRole().getRolePermissions().stream().filter(Objects::nonNull).map(RolePermission::getPermission)
				.filter(Objects::nonNull).map(Permission::getName).anyMatch("super_admin_access"::equals);
	}

	@Override
	public PaginatedResponse<UserDTO> getUsersByCompanyId(Long companyId, int pageNumber, int pageSize, Long userId,
			String search) {
		try {
			if (companyId == null || companyId <= 0) {
				logger.error("SERVICE_LOG | Invalid company ID: {}", companyId);
				throw new BadRequestException("Invalid company ID: " + companyId);
			}

			User actingUser = userRepository.findByIdAndDeletedAtIsNull(userId)
					.orElseThrow(() -> new NotFoundException("User not found: " + userId));

			boolean isSuperAdmin = permissionService.hasPermission(userId, "super_admin_access");
			boolean isAdmin = permissionService.hasPermission(userId, "admin_access");

			if (!isSuperAdmin) {
				Company userCompany = actingUser.getCompany();
				if (userCompany == null || userCompany.getDeletedAt() != null
						|| !userCompany.getId().equals(companyId)) {
					throw new UnauthorizedException("You don't have access to this company’s users.");
				}

				if (!hasPermission(actingUser, userCompany)) {
					throw new UnauthorizedException("You don't have permission to get the user.");
				}
			}

			List<User> users = userRepository.findByCompanyIdAndDeletedAtIsNull(companyId);

			if (isSuperAdmin || isAdmin) {
				users = users.stream()
						.filter(user -> user.getRole().getRolePermissions().stream()
								.noneMatch(role -> role.getRole().getName().equalsIgnoreCase("SUPER_ADMIN")))
						.collect(Collectors.toList());
			}

			if (search != null && !search.isBlank()) {
				String lowerSearch = search.toLowerCase();
				users = users.stream()
						.filter(user -> (user.getName() != null && user.getName().toLowerCase().contains(lowerSearch))
								|| (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerSearch)))
						.collect(Collectors.toList());
			}

			int total = users.size();
			int start = Math.min(pageNumber * pageSize, total);
			int end = Math.min(start + pageSize, total);

			List<UserDTO> dtoList = users.subList(start, end).stream().map(this::mapToDTO).collect(Collectors.toList());

			return new PaginatedResponse<>(dtoList, pageNumber, pageSize, total,
					(int) Math.ceil((double) total / pageSize));

		} catch (BadRequestException | UnauthorizedException | NotFoundException ex) {
			logger.error("SERVICE_LOG | Error: {}", ex.getMessage());
			throw ex;
		} catch (Exception ex) {
			logger.error("SERVICE_LOG | Unexpected error fetching users: {}", ex.getMessage(), ex);
			throw new InternalServerException("Unexpected error fetching users by company");
		} finally {
			MDC.clear();
		}
	}

	@Override
	public PaginatedResponse<UserDTO> filterUsers(Long companyId, String status, Long roleId, LocalDateTime createdFrom,
			LocalDateTime createdTo, int page, int size, Long userId) {
		try {
			User requestingUser = userRepository.findByIdAndDeletedAtIsNull(userId)
					.orElseThrow(() -> new NotFoundException("Requesting user not found"));

			boolean isSuperAdmin = permissionService.hasPermission(userId, "super_admin_access");
			Long userCompanyId = requestingUser.getCompany() != null ? requestingUser.getCompany().getId() : null;

			if (!isSuperAdmin && !permissionService.hasPermission(userId, "admin_access")) {
				throw new UnauthorizedException("User lacks permission to filter users");
			}

			if (!isSuperAdmin) {
				companyId = userCompanyId;
			}

			page = Math.max(page, 0);
			size = size <= 0 ? 10 : Math.min(size, 100);

			User.Status normalizedStatus = null;
			if (StringUtils.isNotBlank(status)) {
				try {
					normalizedStatus = User.Status.valueOf(status.trim().toUpperCase());
				} catch (IllegalArgumentException e) {
					throw new BadRequestException("Invalid status value: " + status);
				}
			}

			Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

			Page<User> userPage;
			userPage = userRepository.filterUsers(companyId, normalizedStatus, roleId, createdFrom, createdTo,
					pageable);

			List<UserDTO> userDTOs = userPage.getContent().stream().map(this::mapToDTOForFilter)
					.filter(Objects::nonNull).collect(Collectors.toList());

			return new PaginatedResponse<>(userDTOs, userPage.getNumber(), userPage.getSize(),
					(int) userPage.getTotalElements(), userPage.getTotalPages());

		} catch (UnauthorizedException | BadRequestException ex) {
			throw ex;
		} catch (Exception ex) {
			logger.error("User filtering failed", ex);
			throw new InternalServerException("Failed to filter users: " + ex.getMessage());
		} finally {
			MDC.clear();
		}
	}

	@Override
	public UserDTO changeUserRole(Long userId, Long roleId, Long requesterId) {
		if (userId == null || userId <= 0 || roleId == null || roleId <= 0 || requesterId == null || requesterId <= 0) {
			throw new BadRequestException("Invalid input parameters");
		}
		User requester = userRepository.findByIdAndDeletedAtIsNull(requesterId)
				.orElseThrow(() -> new NotFoundException("Requester user not found: " + requesterId));
		User targetUser = userRepository.findByIdAndDeletedAtIsNull(userId)
				.orElseThrow(() -> new NotFoundException("Target user not found: " + userId));

		Role newRole = roleRepository.findByIdAndDeletedAtIsNull(roleId)
				.orElseThrow(() -> new NotFoundException("Role not found: " + roleId));
		String oldRoleName = targetUser.getRole() != null ? targetUser.getRole().getName() : "null";
		validateRoleChangePermission(requester, targetUser, newRole);
		targetUser.setRole(newRole);
		targetUser.setUpdatedBy(requesterId);
		targetUser.setUpdatedAt(LocalDateTime.now());

		User updatedUser = userRepository.save(targetUser);
		logger.info("User {} role changed from {} to {} by {}", targetUser.getId(), oldRoleName, newRole.getName(),
				requesterId);

		return mapToDtoForRole(updatedUser);
	}

	@Override
	public PaginatedResponse<UserDTO> getAllUsers(int page, int size, Long userId, String search) {
		try {
			log.info("Fetching users with search='{}', page={}, size={}, requested by userId={}", search, page, size, userId);

			if (page < 0 || size <= 0) {
				throw new BadRequestException("Invalid pagination parameters");
			}

			User actingUser = userRepository.findByIdAndDeletedAtIsNull(userId)
					.orElseThrow(() -> new NotFoundException("User not found"));

			List<User> filteredUsers;

			// Get role name
			String roleName = actingUser.getRole() != null ? actingUser.getRole().getName() : null;
			log.debug("Acting user {} has role: {}", userId, roleName);

			boolean hasFullAccess = roleName != null && (roleName.equals("SUPER_ADMIN") || roleName.equals("HTI_ACCESS"));

			if (hasFullAccess) {
				// SUPER_ADMIN, ADMIN, or HTI_ACCESS can see all users (except other super admins)
				log.info("User {} has {} access. Fetching all non-super-admin users.", userId, roleName);
				filteredUsers = userRepository.findAllNonSuperAdminUsers();
			} else {
				// Other roles: only see users in their own company
				Company company = actingUser.getCompany();
				if (company == null || company.getDeletedAt() != null) {
					log.warn("User {} does not belong to a valid company.", userId);
					throw new NotFoundException("Acting user has no valid company.");
				}

				if (!hasViewPermission(actingUser, company)) {
					log.warn("User {} attempted unauthorized access to company {}", userId, company.getId());
					throw new UnauthorizedException("You don't have permission to view users in this company.");
				}

				log.debug("User {} is allowed to fetch users from company {}", userId, company.getId());
				filteredUsers = userRepository.findByDeletedAtIsNullAndCompanyId(company.getId());
			}

			// Apply search filter
			if (search != null && !search.isBlank()) {
				String lowerSearch = search.toLowerCase();
				filteredUsers = filteredUsers.stream()
						.filter(user -> (user.getName() != null && user.getName().toLowerCase().contains(lowerSearch))
								|| (user.getEmail() != null && user.getEmail().toLowerCase().contains(lowerSearch)))
						.collect(Collectors.toList());
				log.debug("Filtered users count after search='{}': {}", search, filteredUsers.size());
			}

			// Pagination
			int total = filteredUsers.size();
			int start = Math.min(page * size, total);
			int end = Math.min(start + size, total);

			List<UserDTO> pagedUserDTOs = filteredUsers.subList(start, end).stream()
					.map(this::mapToDTO)
					.collect(Collectors.toList());

			log.info("Returning {} users for page={} size={}, total matches={}", pagedUserDTOs.size(), page, size, total);

			return new PaginatedResponse<>(pagedUserDTOs, page, size, total, (int) Math.ceil((double) total / size));

		} catch (NotFoundException | UnauthorizedException | BadRequestException ex) {
			log.warn("BUSINESS_EXCEPTION | {}", ex.getMessage());
			throw ex;
		} catch (Exception ex) {
			log.error("UNEXPECTED_ERROR | Failed to fetch users", ex);
			throw new InternalServerException("Failed to fetch users due to internal error");
		} finally {
			MDC.clear();
		}
	}
	private void validateRoleChangePermission(User requester, User targetUser, Role newRole) {
		if (hasSuperAdminPermission(requester)) {
			return;
		}

		if (hasAdminPermission(requester)) {

			if (!requester.getCompany().getId().equals(targetUser.getCompany().getId())) {
				throw new UnauthorizedException("Admin can only change roles within their own company");
			}

			if (newRole.getType() == Role.Type.DEFAULT) {
				throw new UnauthorizedException("Admin cannot assign DEFAULT roles");
			}

			if (hasAdminPermission(targetUser) && !requester.getId().equals(targetUser.getId())) {
				throw new UnauthorizedException("Admin cannot change other Admins' roles");
			}
			return;
		}

		throw new UnauthorizedException("Insufficient permissions to change user roles");
	}

	private boolean hasSuperAdminPermission(User user) {
		return getPermissions(user).contains("super_admin_access");
	}

	private boolean hasAdminPermission(User user) {
		return getPermissions(user).contains("admin_access") && user.getCompany() != null;
	}

	private Set<String> getPermissions(User user) {
		if (user == null || user.getRole() == null) {
			return Collections.emptySet();
		}

		return Optional.ofNullable(user.getRole().getRolePermissions()).orElse(Collections.emptySet()).stream()
				.filter(Objects::nonNull).map(RolePermission::getPermission).filter(Objects::nonNull)
				.map(Permission::getName).filter(Objects::nonNull).collect(Collectors.toSet());
	}

	private UserDTO mapToDtoForRole(User user) {
		UserDTO dto = new UserDTO();
		dto.setUserId(user.getId());
		dto.setName(user.getName());
		dto.setEmail(user.getEmail());

		if (user.getRole() != null) {
			dto.setRole_Id(user.getRole().getId());
			dto.setRoleName(user.getRole().getName());
		}
		return dto;
	}

	private UserDTO mapToDTOForFilter(User user) {
		UserDTO dto = new UserDTO();
		dto.setUserId(user.getId());
		dto.setEmail(user.getEmail());
		dto.setStatus(user.getStatus().name());

		if (user.getCompany() != null) {
			dto.setCompanyId(user.getCompany().getId());
			dto.setCompanyName(user.getCompany().getName());
		} else {
			dto.setCompanyId(null);
			dto.setCompanyName(null);
		}

		if (user.getRole() != null) {
			dto.setRole_Id(user.getRole().getId());
			dto.setRoleName(user.getRole().getName());
		}

		return dto;
	}

	@Override
	public Map<String, Object> verifyUserToken(String authToken) {
		Map<String, Object> response = new HashMap<>();

		Optional<User> optionalUser = userRepository.findByAuthTokenAndDeletedAtIsNull(authToken);

		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			if (user.getStatus() == User.Status.VPENDING) {
				response.put("status", true);
				response.put("email_id", user.getEmail());
				response.put("user_id", user.getId());
				return response;
			}
		}
		response.put("status", false);
		throw new UnauthorizedException("Invalid or expired token");
	}

	@Override
	public Map<String, Object> assignPassword(String authToken, Long userId, String password) {
		Optional<User> optionalUser = userRepository.findByAuthTokenAndDeletedAtIsNull(authToken);

		if (optionalUser.isEmpty()) {
			throw new UnauthorizedException("Invalid or expired token");
		}

		User user = optionalUser.get();

		if (!user.getId().equals(userId)) {
			throw new UnauthorizedException("User ID does not match token");
		}

		if (user.getStatus() != User.Status.VPENDING) {
			throw new UnauthorizedException("User is not in a valid state to set password");
		}

		user.setPassword(passwordEncoder.encode(password));
		user.setStatus(User.Status.ACTIVE);
		user.setAuthToken(null);
		user.setPasswordChanged(true);
		user.setUpdatedAt(LocalDateTime.now());

		userRepository.save(user);

		try {
			emailService.sendWelcomeEmail(user.getEmail(), user.getName());
		} catch (EmailSendingException e) {
			logger.error("Welcome email failed: {}", e.getMessage());
		}

		Map<String, Object> response = new HashMap<>();
		response.put("status", true);
		response.put("message", "Password set successfully");

		return response;

	}

}