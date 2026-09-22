package com.ocp.pdr.repository;

import com.ocp.pdr.model.AnomalieConsommation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnomalieConsommationRepository extends JpaRepository<AnomalieConsommation, Long> {

    @Query("SELECT DISTINCT a FROM AnomalieConsommation a JOIN FETCH a.article")
    List<AnomalieConsommation> findAllWithArticle();
}
