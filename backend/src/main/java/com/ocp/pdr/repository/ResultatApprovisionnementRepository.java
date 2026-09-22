package com.ocp.pdr.repository;

import com.ocp.pdr.model.ResultatApprovisionnement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultatApprovisionnementRepository extends JpaRepository<ResultatApprovisionnement, Long> {

	@Query("SELECT DISTINCT r FROM ResultatApprovisionnement r JOIN FETCH r.article")
	List<ResultatApprovisionnement> findAllWithArticle();
}
