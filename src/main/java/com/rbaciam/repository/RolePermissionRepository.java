package com.rbaciam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rbaciam.entity.RolePermission;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
  

    List<RolePermission> findByRoleId(Long roleId);

    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.role.id = :roleId")
    void deleteByRoleId(@Param("roleId") Long roleId);

    default void deleteByRoleIdWithLogging(@Param("roleId") Long roleId) {
      
        deleteByRoleId(roleId);
    }
}