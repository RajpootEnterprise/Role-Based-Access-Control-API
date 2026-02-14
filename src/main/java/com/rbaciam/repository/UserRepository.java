package com.rbaciam.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rbaciam.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Logger logger = LoggerFactory.getLogger("DbLogger");

	Optional<User> findByIdAndDeletedAtIsNull(Long id);

	Optional<User> findByEmailAndDeletedAtIsNull(String email);

	@Query("SELECT u FROM User u WHERE " + "u.deletedAt IS NULL AND "
			+ "NOT EXISTS (SELECT 1 FROM RolePermission rp WHERE "
			+ "rp.role = u.role AND rp.permission.name = 'super_admin_access')")
	Page<User> findAllNonSuperAdminUsers(Pageable pageable);

	boolean existsByIdAndCompanyId(@Param("userId") Long userId, @Param("companyId") Long companyId);

	Page<User> findAllByDeletedAtIsNull(Pageable pageable);

	Page<User> findByDeletedAtIsNullAndCompanyId(Long companyId, Pageable pageable);

	Page<User> findByCompanyIdAndDeletedAtIsNull(Long companyId, Pageable pageable);

	@EntityGraph(attributePaths = { "role", "company" })
	@Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
	Page<User> findAllActiveUsers(Pageable pageable);

	@Query("SELECT u FROM User u WHERE u.email = :email AND u.status = :status")
	Optional<User> findByEmailAndStatusWithLogging(@Param("email") String email, @Param("status") String status);

	boolean existsByEmail(String email);

	@Query("SELECT u FROM User u WHERE " + "u.deletedAt IS NULL AND "
			+ "(:companyId IS NULL OR u.company.id = :companyId) AND " + "(:status IS NULL OR u.status = :status) AND "
			+ "(:roleId IS NULL OR (u.role IS NOT NULL AND u.role.id = :roleId)) AND "
			+ "(:createdFrom IS NULL OR u.createdAt >= :createdFrom) AND "
			+ "(:createdTo IS NULL OR u.createdAt <= :createdTo)")
	Page<User> filterUsers(@Param("companyId") Long companyId, @Param("status") User.Status status,
			@Param("roleId") Long roleId, @Param("createdFrom") LocalDateTime createdFrom,
			@Param("createdTo") LocalDateTime createdTo, Pageable pageable);


	Page<User> findByStatusAndRoleId(String status, Long roleId, Pageable pageable);

	Page<User> findByRoleId(Long roleId, Pageable pageable);

	@Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(concat('%', :email,'%'))")
	Page<User> findByEmailContainingIgnoreCase(@Param("email") String email, Pageable pageable);

	@Query("SELECT u FROM User u WHERE u.status = :status")
	Page<User> findByStatus(@Param("status") String status, Pageable pageable);

	@Query("SELECT u FROM User u WHERE u.deletedAt IS NULL AND u.role.name <> 'SUPER_ADMIN'")
	List<User> findAllNonSuperAdminUsers();

	List<User> findByDeletedAtIsNullAndCompanyId(Long companyId);

	List<User> findByCompanyIdAndDeletedAtIsNull(Long companyId);
	
	Optional<User> findByAuthTokenAndDeletedAtIsNull(String authToken);


}