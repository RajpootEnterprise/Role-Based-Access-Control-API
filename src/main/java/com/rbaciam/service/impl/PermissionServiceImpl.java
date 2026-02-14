package com.rbaciam.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rbaciam.dto.PaginatedResponse;
import com.rbaciam.dto.PermissionDTO;
import com.rbaciam.dto.UserDTO;
import com.rbaciam.entity.Permission;
import com.rbaciam.entity.RolePermission;
import com.rbaciam.entity.User;
import com.rbaciam.exception.AuthenticationExceptionFailed;
import com.rbaciam.exception.BadRequestException;
import com.rbaciam.exception.DuplicateException;
import com.rbaciam.exception.InternalServerException;
import com.rbaciam.exception.NotFoundException;
import com.rbaciam.exception.UnauthorizedException;
import com.rbaciam.repository.PermissionRepository;
import com.rbaciam.repository.RolePermissionRepository;
import com.rbaciam.repository.UserRepository;
import com.rbaciam.service.PermissionService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private static final Logger logger = LoggerFactory.getLogger("TraceLogger");

/*    @Override
    @Transactional
    @CacheEvict(value = "permissions", allEntries = true)
    public PermissionDTO createPermission(PermissionDTO permissionDTO, Long userId) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        MDC.put("userId", String.valueOf(userId));
        try {
            if (userId == null || userId <= 0) {
                throw new AuthenticationExceptionFailed("Invalid user ID: " + userId);
            }
            if (!hasPermission(userId, "super_admin_access")) {
                throw new UnauthorizedException("User lacks permission to create permission");
            }
            validatePermissionDTO(permissionDTO);
            if (permissionRepository.findByNameAndDeletedAtIsNull(permissionDTO.getName()).isPresent()) {
                throw new DuplicateException("Permission name already exists: " + permissionDTO.getName());
            }
            Permission permission = mapToEntity(permissionDTO);
            logger.info("TRACE_LOG | Action=CREATE_PERMISSION | Name={} | UserId={}", permission.getName(), userId);
            permission.setCreatedAt(LocalDateTime.now());
            permission.setUpdatedAt(LocalDateTime.now());
            permission.setCreatedBy(userId);
            permission.setUpdatedBy(userId);
            Permission savedPermission = permissionRepository.save(permission);
            return mapToDTO(savedPermission);
        } catch (AuthenticationExceptionFailed ex) {
            throw ex;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (DuplicateException ex) {
            throw ex;
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new InternalServerException("Failed to create permission due to database error");
        } finally {
            MDC.clear();
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "permissions", key = "#id")
    public PermissionDTO updatePermission(Long id, PermissionDTO permissionDTO, Long userId) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        MDC.put("userId", String.valueOf(userId));
        try {
            if (id == null || id <= 0) {
                throw new BadRequestException("Invalid permission ID: " + id);
            }
            if (userId == null || userId <= 0) {
                throw new AuthenticationExceptionFailed("Invalid user ID: " + userId);
            }
            if (!hasPermission(userId, "super_admin_access")) {
                throw new UnauthorizedException("User lacks permission to update permission");
            }
            validatePermissionDTO(permissionDTO);
            Permission permission = permissionRepository.findByIdAndDeletedAtIsNull(id)
                    .orElseThrow(() -> new NotFoundException("Permission not found: " + id));
            permission.setName(permissionDTO.getName());
            permission.setUpdatedAt(LocalDateTime.now());
            permission.setUpdatedBy(userId);
            logger.info("TRACE_LOG | Action=UPDATE_PERMISSION | ID={} | Name={} | UserId={}", id, permission.getName(), userId);
            Permission updatedPermission = permissionRepository.save(permission);
            return mapToDTO(updatedPermission);
        } catch (AuthenticationExceptionFailed ex) {
            throw ex;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (NotFoundException ex) {
            throw ex;
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new InternalServerException("Failed to update permission due to database error");
        } finally {
            MDC.clear();
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "permissions", key = "#id")
    public void deletePermission(Long id, Long userId) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        MDC.put("userId", String.valueOf(userId));
        try {
            if (id == null || id <= 0) {
                throw new BadRequestException("Invalid permission ID: " + id);
            }
            if (userId == null || userId <= 0) {
                throw new AuthenticationExceptionFailed("Invalid user ID: " + userId);
            }
            if (!hasPermission(userId, "super_admin_access")) {
                throw new UnauthorizedException("User lacks permission to delete permission");
            }
            Permission permission = permissionRepository.findByIdAndDeletedAtIsNull(id)
                    .orElseThrow(() -> new NotFoundException("Permission not found: " + id));
            permission.setDeletedAt(LocalDateTime.now());
            permission.setDeletedBy(userId);
            logger.info("TRACE_LOG | Action=DELETE_PERMISSION | ID={} | UserId={}", id, userId);
            permissionRepository.save(permission);
        } catch (AuthenticationExceptionFailed ex) {
            throw ex;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (NotFoundException ex) {
            throw ex;
        } catch (UnauthorizedException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new InternalServerException("Failed to delete permission due to database error");
        } finally {
            MDC.clear();
        }
    }
    */
    @Override
    @Cacheable(value = "permissions", key = "#id")
    public Optional<PermissionDTO> getPermissionById(Long id, Long userId) {
    	 validateUserAccess(userId);
        try {
            if (id == null || id <= 0) {
                throw new BadRequestException("Invalid permission ID: " + id);
            }
            logger.info("TRACE_LOG | Action=GET_PERMISSION_BY_ID | ID={}", id);
           
            return permissionRepository.findById(id)
                    .map(this::mapToDTO);
        } catch (BadRequestException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new InternalServerException("Failed to fetch permission due to database error");
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
    @Cacheable(value = "permissions")
    public PaginatedResponse<PermissionDTO> getAllPermissions(int page, int size, Long userId) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        validateUserAccess(userId);

        try {
            if (page < 0 || size <= 0) {
                throw new BadRequestException("Invalid page or size parameters");
            }

            Pageable pageable = PageRequest.of(page, size);
            logger.info("TRACE_LOG | Action=GET_ALL_PERMISSIONS | Page={}, Size={}, UserId={}", page, size, userId);

            // Exclude permissions like "super_admin_access"
            Page<Permission> permissionPage = permissionRepository.findByNameNot(
                    "super_admin_access", pageable);
            
            List<PermissionDTO> dtoList = permissionPage.getContent().stream()
                    .map(this::mapToDTO)
                    .toList();

            return new PaginatedResponse<>(
                    dtoList,
                    permissionPage.getNumber(),
                    permissionPage.getSize(),
                    (int) permissionPage.getTotalElements(),
                    permissionPage.getTotalPages()
            );

        } catch (BadRequestException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new InternalServerException("Failed to fetch permissions due to database error");
        } finally {
            MDC.clear();
        }
    }

//    @Override
//    @Cacheable(value = "permissions")
//    public PaginatedResponse<PermissionDTO> getAllPermissions(int page,int size,Long userId) {
//    	 String requestId = UUID.randomUUID().toString();
//    	    MDC.put("requestId", requestId);
//    	     validateUserAccess(userId); 
//
//    	    try {
//    	        if (page < 0 || size <= 0) {
//    	            throw new BadRequestException("Invalid page or size parameters");
//    	        }
//    	        Pageable pageable = PageRequest.of(page, size);
//    	        logger.info("TRACE_LOG | Action=GET_ALL_PERMISSIONS | Page={}, Size={}, UserId={}", page, size, userId);
//
//    	        Page<Permission> permissionPage = permissionRepository.findAllByDeletedAtIsNull(pageable);
//    	        List<PermissionDTO> dtoList = permissionPage.getContent().stream()
//    	                .map(this::mapToDTO)
//    	                .toList();
//    	        return new PaginatedResponse<>(
//    	                dtoList,
//    	                permissionPage.getNumber(),
//    	                permissionPage.getSize(),
//    	                (int) permissionPage.getTotalElements(),
//    	                permissionPage.getTotalPages()
//    	        );
//    	        
//    	    } catch (BadRequestException ex) {
//    	        throw ex;
//    	    } catch (DataAccessException ex) {
//    	        throw new InternalServerException("Failed to fetch permissions due to database error");
//    	    } finally {
//    	        MDC.clear();
//    	    }
//    	}

    @Override
    public Page<PermissionDTO> filterPermissions(String name, Pageable pageable) {
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        try {
            if (pageable == null) {
                throw new BadRequestException("Pageable cannot be null");
            }
            logger.info("TRACE_LOG | Action=FILTER_PERMISSIONS | Name={}", name);
            return permissionRepository.findByNameWithLogging(name, pageable)
                    .map(this::mapToDTO);
        } catch (BadRequestException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new InternalServerException("Failed to filter permissions due to database error");
        } finally {
            MDC.clear();
        }
    }

    @Override
    public boolean hasPermission(Long userId, String permissionName) {
        try {
            System.out.println("Checking permission for user: " + userId);
            if (userId == null || userId <= 0) {
                throw new BadRequestException("Invalid user ID: " + userId);
            }

            if (permissionName == null || permissionName.trim().isEmpty()) {
                throw new BadRequestException("Permission name is required");
            }

            logger.info("TRACE_LOG | Action=CHECK_PERMISSION | Permission={} | UserId={}", permissionName, userId);

            boolean result = userRepository.findByIdAndDeletedAtIsNull(userId)
                .map(user1 -> {
                    Long roleId = user1.getRole().getId();
                    return rolePermissionRepository.findByRoleId(roleId).stream()
                        .anyMatch(rp -> {
                            System.out.println("RolePermission: " + rp.getPermission().getName());
                            return rp.getPermission().getName().equals(permissionName);
                        });
                }).orElse(false);

            System.out.println("Permission check result: " + result);
            return result;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (DataAccessException ex) {
            throw new InternalServerException("Failed to check permission due to database error");
        } finally {
            MDC.clear();
        }
    }

    private void validatePermissionDTO(PermissionDTO permissionDTO) {
        if (permissionDTO == null) {
            throw new BadRequestException("Permission DTO cannot be null");
        }
        if (permissionDTO.getName() == null || permissionDTO.getName().trim().isEmpty()) {
            throw new BadRequestException("Permission name is required");
        }
    }

    private Permission mapToEntity(PermissionDTO permissionDTO) {
        Permission permission = new Permission();
        permission.setName(permissionDTO.getName());
        return permission;
    }

    private PermissionDTO mapToDTO(Permission permission) {
        PermissionDTO permissionDTO = new PermissionDTO();
      //  permissionDTO.setId(permission.getId());
        permissionDTO.setName(permission.getName());
        return permissionDTO;
    }
}