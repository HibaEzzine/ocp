package com.ocp.pdr.repository;

import com.ocp.pdr.model.ImportDonnees;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportDonneesRepository extends JpaRepository<ImportDonnees, Long> {
}

