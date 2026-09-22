package com.ocp.pdr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ocp.pdr.model.FicheBOM;

@Repository
public interface FicheBOMRepository extends JpaRepository<FicheBOM, Long> {
}
