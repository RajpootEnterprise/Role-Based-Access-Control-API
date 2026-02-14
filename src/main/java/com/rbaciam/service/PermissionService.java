package com.rbaciam.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.rbaciam.dto.PaginatedResponse;
import com.rbaciam.dto.PermissionDTO;

import java.util.Optional;

public interface PermissionService {
//    PermissionDTO createPermission(PermissionDTO permissionDTO, Long userId);
//    PermissionDTO updatePermission(Long id, PermissionDTO permissionDTO, Long userId);
//    void deletePermission(Long id, Long userId);
    Optional<PermissionDTO> getPermissionById(Long id,Long userId);
    PaginatedResponse<PermissionDTO> getAllPermissions(int page,int size,Long userId);
    Page<PermissionDTO> filterPermissions(String name, Pageable pageable);
    boolean hasPermission(Long userId, String permissionName);
}