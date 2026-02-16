package com.rbaciam.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.rbaciam.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import com.rbaciam.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

	@Autowired
	private RoleService roleService;
	private static final Logger logger = LoggerFactory.getLogger("TraceLogger");

	@Operation(summary = "Assign permissions to role", description = "Accessible by Super Admin only")
	@ApiResponse(responseCode = "200", description = "Permissions assigned successfully")
	@ApiResponse(responseCode = "400", description = "Invalid input")
	@ApiResponse(responseCode = "403", description = "Forbidden - Only Super Admin can assign permissions")
	@PostMapping("/assign_permissions")
	public ResponseEntity<ApiResponseDto> assignPermissionsToRole(
			@Valid @RequestBody AssignPermissionsRequestDTO request,
			@RequestHeader("X-User-Id") Long userId) {

		logger.info("CONTROLLER_LOG | Assigning permissions to role ID={} by userId={}",
				request.getRoleId(), userId);

		roleService.assignPermissionsToRole(request.getRoleId(), request.getPermissionIds(), userId);

		return ResponseEntity.ok(new ApiResponseDto(true, "Permissions assigned successfully"));
	}

	@Operation(summary = "Remove permissions from role", description = "Accessible by Super Admin only")
	@ApiResponse(responseCode = "200", description = "Permissions removed successfully")
	@ApiResponse(responseCode = "400", description = "Invalid input")
	@ApiResponse(responseCode = "403", description = "Forbidden - Only Super Admin can remove permissions")
	@PostMapping("/remove_permissions")
	public ResponseEntity<ApiResponseDto> removePermissionsFromRole(
			@Valid @RequestBody AssignPermissionsRequestDTO request,
			@RequestHeader("X-User-Id") Long userId) {

		logger.info("CONTROLLER_LOG | Removing permissions from role ID={} by userId={}",
				request.getRoleId(), userId);

		roleService.removePermissionsFromRole(request.getRoleId(), request.getPermissionIds(), userId);

		return ResponseEntity.ok(new ApiResponseDto(true, "Permissions removed successfully"));
	}

	@Operation(summary = "Get permissions for a role", description = "Accessible by Super Admin and Admin")
	@ApiResponse(responseCode = "200", description = "Permissions retrieved successfully")
	@GetMapping("/get_permissions/{role_id}")
	public ResponseEntity<List<PermissionDTO>> getRolePermissions(
			@PathVariable("role_id") Long roleId,
			@RequestHeader("X-User-Id") Long userId) {

		logger.info("CONTROLLER_LOG | Getting permissions for role ID={}", roleId);

		List<PermissionDTO> permissions = roleService.getRolePermissions(roleId, userId);

		return ResponseEntity.ok(permissions);
	}

	@Operation(summary = "Create a new role", description = "Accessible by Super Admin or Admin")
	@ApiResponse(responseCode = "200", description = "Role created successfully")
	@ApiResponse(responseCode = "400", description = "Invalid input")
	@PostMapping("/create_role")
	public ResponseEntity<ApiResponseDto> createRole(@Valid @RequestBody CreateRoleDTO roleDTO,@RequestHeader("X-User-Id") Long userId) {
		roleService.createRole(roleDTO, userId);
		return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponseDto(true, "Role is created"));
	}

	@Operation(summary = "Update a role", description = "Accessible by Super Admin or Admin, default roles only by Super Admin")
	@ApiResponse(responseCode = "200", description = "Role updated successfully")
	@ApiResponse(responseCode = "404", description = "Role not found")
	@PutMapping("/{role_id}")
	public ResponseEntity<String> updateRole(@PathVariable("role_id") Long id, @Valid @RequestBody CreateRoleDTO roleDTO,
			Long userId) {
		String requestId = UUID.randomUUID().toString();
		MDC.put("requestId", requestId);
		MDC.put("userId", String.valueOf(userId));
		try {
			logger.info("CONTROLLER_LOG | Processing update role request for ID={}", id);
			roleService.updateRole(id, roleDTO, userId);
			return ResponseEntity.ok("updated role successfully");
		} finally {
			MDC.clear();
		}
	}

	@Operation(summary = "Delete a role", description = "Accessible by Super Admin, default roles only by Super Admin")
	@ApiResponse(responseCode = "204", description = "Role deleted successfully")
	@ApiResponse(responseCode = "404", description = "Role not found")
	@DeleteMapping("/{role_id}")
	public ResponseEntity<String> deleteRole(@PathVariable("role_id") Long id, Long userId) {
		String requestId = UUID.randomUUID().toString();
		MDC.put("requestId", requestId);
		MDC.put("userId", String.valueOf(userId));
		try {
			logger.info("CONTROLLER_LOG | Processing delete role request for ID={}", id);
			System.out.println(id + " id and role is printed" + userId);
			roleService.deleteRole(id, userId);
			return ResponseEntity.ok("Deleted Role Successfully");
		} finally {
			MDC.clear();
		}
	}

	@GetMapping
	@Operation(summary = "Get all roles", description = "Returns paginated list of roles")
	@ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
	public ResponseEntity<PaginatedResponse<RoleDTO>> getAllRoles(
	        @RequestHeader("X-USER-ID") Long userId,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size,
	        @RequestParam(required = false) String search) {

	    PaginatedResponse<RoleDTO> response = roleService.getAllRoles(userId, page, size, search);
	    return ResponseEntity.ok(response);
	}


	@Operation(summary = "Get a role by ID", description = "Accessible to SUPER_ADMIN and ADMIN users only")
	@ApiResponse(responseCode = "200", description = "Role retrieved successfully")
	@ApiResponse(responseCode = "404", description = "Role not found")
	@GetMapping("/{role_id}")
	public ResponseEntity<RoleDTO> getRoleById(
	        @RequestHeader("X-USER-ID") Long userId,
	        @PathVariable("role_id") Long roleId) {
	    logger.info("CONTROLLER_LOG | User {} requested role by ID={}", userId, roleId);
	    return roleService.getRoleById(userId,roleId).map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}


	@Operation(summary = "roles search filter by fields", description = "Filter roles by name, permissionId, createdFrom, createdTo")
	@ApiResponse(responseCode = "200", description = "Filtered roles retrieved successfully")
	@GetMapping("/filter")
	public ResponseEntity<PaginatedResponse<RoleDTO>> filterRoles(
	        @RequestParam(required = false) String roleName,
	        @RequestParam(required = false) Long permissionId,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdFrom,
	        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdTo,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size,
	        @RequestHeader("X-User-Id") Long userId) {

	        return ResponseEntity.ok(roleService.filterRoles(roleName,permissionId,createdFrom,createdTo, page, size, userId));
	}
}