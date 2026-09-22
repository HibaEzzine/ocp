package com.ocp.pdr.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ocp.pdr.model.Consommation;

@Repository
public interface ConsommationRepository extends JpaRepository<Consommation, Long> {
	List<Consommation> findByDatasetType(String datasetType);

	List<Consommation> findByDatasetTypeAndPeriodeImport(String datasetType, String periodeImport);
}
