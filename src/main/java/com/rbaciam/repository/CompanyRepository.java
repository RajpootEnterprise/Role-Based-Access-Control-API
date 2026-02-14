package com.rbaciam.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rbaciam.entity.Company;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
	Logger logger = LoggerFactory.getLogger("DbLogger");

	Optional<Company> findByIdAndDeletedAtIsNull(Long id);

	Optional<Company> findByDomainAndDeletedAtIsNull(String domain);

	Page<Company> findAllByDeletedAtIsNull(Pageable pageable);
	
	@Query("SELECT c FROM Company c WHERE " +
	       "(:name IS NULL OR c.name LIKE %:name%) AND " +
	       "(:domain IS NULL OR c.domain LIKE %:domain%) AND " +
	       "c.deletedAt IS NULL")
	Page<Company> searchCompanies(
	    @Param("name") String name,
	    @Param("domain") String domain,
	    Pageable pageable);

	@Query("SELECT c FROM Company c WHERE c.deletedAt IS NULL AND " + "(:name IS NULL OR c.name LIKE %:name%) AND "
			+ "(:country IS NULL OR c.country = :country)")
	Page<Company> findByNameAndCountry(@Param("name") String name, @Param("country") String country, Pageable pageable);

	@Query("SELECT COUNT(u) FROM User u WHERE u.company.id = :companyId AND u.deletedAt IS NULL")
	int countByCompanyIdAndDeletedAtIsNull(@Param("companyId") Long companyId);

	List<Company> findAllByDeletedAtIsNull(Sort ascending);

	List<Company> findByDeletedAtIsNullAndId(Long companyId);
	
	@Query("SELECT c FROM Company c WHERE " +
		       "(:country IS NULL OR LOWER(c.country) LIKE %:country%) AND " +
		       "(:industry IS NULL OR LOWER(c.industry) LIKE %:industry%) AND " +
		       "(:createdFrom IS NULL OR c.createdAt >= :createdFrom) AND " +
		       "(:createdTo IS NULL OR c.createdAt <= :createdTo) AND " +
		       "c.deletedAt IS NULL")
		Page<Company> findByFilters(
		        @Param("country") String country,
		        @Param("industry") String industry,
		        @Param("createdFrom") LocalDateTime createdFrom,
		        @Param("createdTo") LocalDateTime createdTo,
		        Pageable pageable);

	

	
}