package com.ocp.pdr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ocp.pdr.model.ImportExcelDA;

@Repository
public interface ImportExcelDARepository extends JpaRepository<ImportExcelDA, Long> {
}
