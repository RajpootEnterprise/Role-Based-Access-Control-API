package com.rbaciam.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.rbaciam.dto.CreateRoleDTO;
import com.rbaciam.dto.PaginatedResponse;
import com.rbaciam.dto.PermissionDTO;
import com.rbaciam.dto.RoleDTO;

public interface RoleService {
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds, Long userId);
    void removePermissionsFromRole(Long roleId, List<Long> permissionIds, Long userId);
    List<PermissionDTO> getRolePermissions(Long roleId, Long userId);
	CreateRoleDTO createRole(CreateRoleDTO roleDTO, Long userId);
	CreateRoleDTO updateRole(Long id, CreateRoleDTO roleDTO, Long userId);
    void deleteRole(Long id, Long userId);
    Optional<RoleDTO> getRoleById(Long userId,Long roleId);
    PaginatedResponse<RoleDTO> getAllRoles(Long userId,int page,int size,String search);
    PaginatedResponse<RoleDTO> filterRoles(String name, Long permissionId, LocalDateTime createdFrom, LocalDateTime createdTo, int page, int size, Long userId);

}