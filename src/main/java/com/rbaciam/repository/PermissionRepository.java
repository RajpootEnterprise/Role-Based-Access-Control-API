package com.rbaciam.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rbaciam.entity.Permission;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Logger logger = LoggerFactory.getLogger("DbLogger");
//two methods are working findbyId and findbyNameNot

    List<Permission> findAllById(Iterable<Long> ids);

    Optional<Permission> findById(Long id); 

    Optional<Permission> findByName(String name); 

    Page<Permission> findAll(Pageable pageable); 
    
    Page<Permission> findByNameNot(String name, Pageable pageable); 

    default Page<Permission> findByNameWithLogging(String name, Pageable pageable) {
        logger.info("DB_LOG | Executing findByName with name={}", name);
        return name == null ? findAll(pageable) : findAll(pageable);  
    }
}