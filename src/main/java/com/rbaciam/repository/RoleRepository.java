package com.rbaciam.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rbaciam.entity.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	Logger logger = LoggerFactory.getLogger("DbLogger");

	Optional<Role> findByIdAndDeletedAtIsNull(Long id);

	Optional<Role> findByNameAndDeletedAtIsNull(String name);

	Page<Role> findAllByDeletedAtIsNull(Pageable pageable);

	Page<Role> findByDeletedAtIsNullAndNameNot(String name, Pageable pageable);

	@Query(value = "SELECT r FROM Role r WHERE r.deletedAt IS NULL")
	Page<Role> findAllWithoutFetch(Pageable pageable);

	@Query("SELECT DISTINCT r FROM Role r " + "LEFT JOIN r.rolePermissions rp " + "LEFT JOIN rp.permission p "
			+ "WHERE r.deletedAt IS NULL " + "AND (:name IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :name, '%'))) "
			+ "AND (:permissionId IS NULL OR p.id = :permissionId) "
			+ "AND (:createdFrom IS NULL OR r.createdAt >= :createdFrom) "
			+ "AND (:createdTo IS NULL OR r.createdAt <= :createdTo)")
	Page<Role> filterRolesByCriteria(@Param("name") String name, @Param("permissionId") Long permissionId,
			@Param("createdFrom") LocalDateTime createdFrom, @Param("createdTo") LocalDateTime createdTo,
			Pageable pageable);

	Page<Role> findByDeletedAtIsNullAndNameNotAndNameContainingIgnoreCase(String excludedName, String search,
			Pageable pageable);

}