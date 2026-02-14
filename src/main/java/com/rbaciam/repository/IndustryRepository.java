package com.rbaciam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rbaciam.entity.Industry;

import java.util.List;

@Repository
public interface IndustryRepository extends JpaRepository<Industry, Long> {
    List<Industry> findAll();
}
