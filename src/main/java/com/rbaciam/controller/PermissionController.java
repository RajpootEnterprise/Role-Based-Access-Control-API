package com.rbaciam.controller;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import com.rbaciam.dto.PaginatedResponse;
import com.rbaciam.dto.PermissionDTO;
import com.rbaciam.service.PermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {
	private final PermissionService permissionService;
	private static final Logger logger = LoggerFactory.getLogger("TraceLogger");

/*	@Operation(summary = "Create a new permission", description = "Accessible by Super Admin or Admin")
	@ApiResponse(responseCode = "200", description = "Permission created successfully")
	@ApiResponse(responseCode = "400", description = "Invalid input")
	@PostMapping("/create_permission")
	public ResponseEntity<String> createPermission(@Valid @RequestBody PermissionDTO permissionDTO, Long userId) {
		String requestId = UUID.randomUUID().toString();
		MDC.put("requestId", requestId);
		MDC.put("userId", String.valueOf(userId));
		logger.info("CONTROLLER_LOG | Processing create permission request for Name={}", permissionDTO.getName());
		permissionService.createPermission(permissionDTO, userId);
		MDC.clear();
		return ResponseEntity.ok("create permission successfully");
	}

	@Operation(summary = "Update a permission", description = "Accessible by Super Admin or Admin")
	@ApiResponse(responseCode = "200", description = "Permission updated successfully")
	@ApiResponse(responseCode = "404", description = "Permission not found")
	@PutMapping("/{permission_id}")
	public ResponseEntity<String> updatePermission(@PathVariable("permission_id") Long id,
			@Valid @RequestBody PermissionDTO permissionDTO, Long userId) {
		String requestId = UUID.randomUUID().toString();
		MDC.put("requestId", requestId);
		MDC.put("userId", String.valueOf(userId));
		logger.info("CONTROLLER_LOG | Processing update permission request for ID={}", id);
		permissionService.updatePermission(id, permissionDTO, userId);
		MDC.clear();
		return ResponseEntity.ok("update user successfully");
	}

	@Operation(summary = "Delete a permission", description = "Accessible by Super Admin or Admin")
	@ApiResponse(responseCode = "204", description = "Permission deleted successfully")
	@ApiResponse(responseCode = "404", description = "Permission not found")
	@DeleteMapping("/{permission_id}")
	public ResponseEntity<String> deletePermission(@PathVariable("permission_id") Long id, Long userId) {
		String requestId = UUID.randomUUID().toString();
		MDC.put("requestId", requestId);
		MDC.put("userId", String.valueOf(userId));
		logger.info("CONTROLLER_LOG | Processing delete permission request for ID={}", id);
		permissionService.deletePermission(id, userId);
		MDC.clear();
		return ResponseEntity.ok("permission is deleted");
	}
   */
	@Operation(summary = "Get all permissions", description = "Returns paginated list of permissions")
	@ApiResponse(responseCode = "200", description = "Permissions retrieved successfully")
	@GetMapping
	public ResponseEntity<PaginatedResponse<PermissionDTO>> getAllPermissions(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size,
	        @RequestHeader("X-User-Id") Long userId) {
	    PaginatedResponse<PermissionDTO> response = permissionService.getAllPermissions(page,size,userId);
	    return ResponseEntity.ok(response);
	}

	@Operation(summary = "Get permissionById", description = "Returns paginated list of permissions")
	@ApiResponse(responseCode = "200", description = "Permissions retrieved successfully")
	@GetMapping("/{userId}")
	public ResponseEntity<PermissionDTO> getPermissionById(@RequestParam(defaultValue = "Permission_Id")Long id,
			@RequestHeader("X-User-Id") Long userId){
		return  permissionService.getPermissionById(id, userId).map(ResponseEntity::ok)
				.orElseGet(()->ResponseEntity.notFound().build());
		
	}

	@Operation(summary = "Filter permissions", description = "Filter permissions by name")
	@ApiResponse(responseCode = "200", description = "Filtered permissions retrieved successfully")
	@GetMapping("/filter")
	public ResponseEntity<Page<PermissionDTO>> filterPermissions(@RequestParam(required = false) String name,
			Pageable pageable) {
		String requestId = UUID.randomUUID().toString();
		MDC.put("requestId", requestId);
		logger.info("CONTROLLER_LOG | Processing filter permissions request with name={}", name);
		ResponseEntity<Page<PermissionDTO>> response = ResponseEntity
				.ok(permissionService.filterPermissions(name, pageable));
		MDC.clear();
		return response;
	}
}