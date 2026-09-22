package com.ocp.pdr.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ocp.pdr.model.HistoriqueTraitement;

@Repository
public interface HistoriqueTraitementRepository extends JpaRepository<HistoriqueTraitement, Long> {
	Optional<HistoriqueTraitement> findTopByOperationAndStatutOrderByDateOperationDesc(String operation, String statut);
}
