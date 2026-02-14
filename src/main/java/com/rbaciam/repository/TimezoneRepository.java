package com.rbaciam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rbaciam.entity.Timezone;

import java.util.List;

@Repository
public interface TimezoneRepository extends JpaRepository<Timezone, Long> {
    List<Timezone> findAll();
}