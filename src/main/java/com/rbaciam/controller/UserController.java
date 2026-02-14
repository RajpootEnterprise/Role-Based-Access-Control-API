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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rbaciam.dto.ApiResponseDto;
import com.rbaciam.dto.AssignPasswordRequestDTO;
import com.rbaciam.dto.PaginatedResponse;
import com.rbaciam.dto.UserCreateDTO;
import com.rbaciam.dto.UserDTO;
import com.rbaciam.dto.UserUpdateDTO;
import com.rbaciam.exception.NotFoundException;
import com.rbaciam.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;

	private static final Logger logger = LoggerFactory.getLogger("TraceLogger");

	@Operation(summary = "Create a new user", description = "Accessible by Super Admin or Admin")
	@ApiResponse(responseCode = "200", description = "User created successfully")
	@ApiResponse(responseCode = "400", description = "Invalid input")
	@PostMapping("/create_user")
	public ResponseEntity<ApiResponseDto> createUser(@Valid @RequestBody UserCreateDTO userCreateDTO,
			@RequestHeader("X-User-Id") Long userId) {
		Map<String, Object> createData = userService.createUser(userCreateDTO, userId);
		logger.info("Request to create user by userId {}: {}", userId, userCreateDTO);
		Long createdId = (Long) createData.get("id");
		logger.info("User created successfully for userId {}", userId);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(new ApiResponseDto(createdId, true, "User Created Successfully"));
	}

	@Operation(summary = "verify-token", description = "verify token by Super Admin or Admin")
	@ApiResponse(responseCode = "200", description = "validate  token successfully")
	@ApiResponse(responseCode = "401", description = "Invalid or expired token")
	@GetMapping("/verify-token")
	public ResponseEntity<Map<String, Object>> verifyUserToken(@RequestHeader("auth_token") String authToken) {
		Map<String, Object> response = userService.verifyUserToken(authToken);
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "assign-password", description = "assign password by Super Admin Admin and User")
	@ApiResponse(responseCode = "200", description = "password  created successfully")
	@ApiResponse(responseCode = "401", description = "Invalid or expired token")
	@PostMapping("/assign-password")
	public ResponseEntity<Map<String, Object>> assignPassword(@RequestHeader("auth_token") String authToken,
			@RequestBody @Valid AssignPasswordRequestDTO request) {
		Map<String, Object> response = userService.assignPassword(authToken, request.getUserId(),
				request.getPassword());
		return ResponseEntity.ok(response);

	}

	@Operation(summary = "Update a user", description = "Accessible by Super Admin or Admin for their company")
	@ApiResponse(responseCode = "200", description = "User updated successfully")
	@PutMapping("/{user_id}")
	public ResponseEntity<ApiResponseDto> updateUser(@PathVariable("user_id") Long userIdToUpdate,
			@RequestHeader("X-User-Id") Long requesterId, @Valid @RequestBody UserUpdateDTO updateDTO) {

		logger.info("Request to update user [targetId={}, performedBy={}]", userIdToUpdate, requesterId);
		Map<String, Object> updateData = userService.updateUser(userIdToUpdate, updateDTO, requesterId);
		Long updatedId = (Long) updateData.get("id");
		logger.info("User updated successfully [targetId={}]", userIdToUpdate);

		return ResponseEntity.ok(new ApiResponseDto(updatedId, true, "User Updated Successfully"));
	}

	@Operation(summary = "Delete a user", description = "Accessible by Super Admin or Admin for their company")
	@ApiResponse(responseCode = "200", description = "User deleted successfully")
	@DeleteMapping("/{user_id}")
	public ResponseEntity<ApiResponseDto> deleteUser(@PathVariable("user_id") Long targetUserId,
			@RequestHeader("X-User-Id") Long requesterId) {
		logger.info("Request to delete user [targetId={}, performedBy={}]", targetUserId, requesterId);
		userService.deleteUser(targetUserId, requesterId);
		logger.info("User deleted successfully [targetId={}]", targetUserId);
		return ResponseEntity.ok(new ApiResponseDto(true, "User deleted successfully"));
	}

	@Operation(summary = "Get a user by ID", description = "Accessible by: Super Admin (any user), Admin (same company users), User (own profile only)")
	@ApiResponse(responseCode = "200", description = "User retrieved successfully")
	@ApiResponse(responseCode = "403", description = "Forbidden - No access to this user")
	@ApiResponse(responseCode = "404", description = "User not found")
	@GetMapping("/{user_id}")
	public ResponseEntity<UserDTO> getUserById(@PathVariable("user_id") Long targetUserId,
			@RequestHeader("X-User-Id") Long requestingUserId) {

		logger.info("CONTROLLER_LOG | Request to fetch user by ID [requestingUserId={}, targetUserId={}]",
				requestingUserId, targetUserId);

		UserDTO userDTO = userService.getUserById(targetUserId, requestingUserId)
				.orElseThrow(() -> new NotFoundException("User not found"));

		return ResponseEntity.ok(userDTO);
	}

	@Operation(summary = "Get users by company ID", description = "Returns paginated list of users for a specific company")
	@ApiResponse(responseCode = "200", description = "Users retrieved successfully")
	@GetMapping("/company/{company_id}")
	public ResponseEntity<PaginatedResponse<UserDTO>> getUsersByCompanyId(@PathVariable("company_id") Long companyId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestHeader("X-User-Id") Long userId, @RequestParam(required = false) String search) {
		PaginatedResponse<UserDTO> response = userService.getUsersByCompanyId(companyId, page, size, userId, search);
		return ResponseEntity.ok(response);
	}

	@PatchMapping("/user_role/{userId}")
	@Operation(summary = "Change user role", description = "Super Admin can change any role. Admin can only change roles within their company")
	@ApiResponse(responseCode = "200", description = "Role changed successfully")
	@ApiResponse(responseCode = "403", description = "Forbidden - insufficient permissions")
	@ApiResponse(responseCode = "404", description = "User or Role not found")
	public ResponseEntity<UserDTO> changeUserRole(@PathVariable Long userId, @RequestParam Long roleId,
			@RequestHeader("X-Requester-ID") Long requesterId) {

		UserDTO updatedUser = userService.changeUserRole(userId, roleId, requesterId);
		return ResponseEntity.ok(updatedUser);
	}

	@Operation(summary = "users search filter by fields", description = "Filter users by companyId, status, role, and createdAt range")
	@ApiResponse(responseCode = "200", description = "Filtered users retrieved successfully")
	@GetMapping("/filter")
	public PaginatedResponse<UserDTO> filterUsers(@RequestParam(required = false) Long companyId,
			@RequestParam(required = false) String status, @RequestParam(required = false) Long roleId,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestHeader("X-User-Id") Long userId) {
		return userService.filterUsers(companyId, status, roleId, createdFrom, createdTo, page, size, userId);
	}

	@GetMapping("/search")
	@Operation(summary = "Get all users search", description = "Returns paginated list of users with optional search")
	@ApiResponse(responseCode = "200", description = "Users retrieved successfully")
	public ResponseEntity<PaginatedResponse<UserDTO>> getUsers(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(required = false) String search,
			@RequestHeader("X-User-Id") Long userId) {

		PaginatedResponse<UserDTO> response = userService.getAllUsers(page, size, userId, search);
		return ResponseEntity.ok(response);
	}

}