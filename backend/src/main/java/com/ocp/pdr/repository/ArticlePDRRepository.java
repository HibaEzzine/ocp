package com.ocp.pdr.repository;

import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.enums.GroupeHomogene;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticlePDRRepository extends JpaRepository<ArticlePDR, Long> {
    Optional<ArticlePDR> findByCodeSAP(String codeSAP);
    boolean existsByCodeSAP(String codeSAP);

    // Pagination et filtrage
    Page<ArticlePDR> findAll(Pageable pageable);
    
    Page<ArticlePDR> findByGroupeHomogene(GroupeHomogene groupeHomogene, Pageable pageable);
    
    @Query("SELECT a FROM ArticlePDR a WHERE LOWER(a.description) LIKE LOWER(CONCAT('%', :term, '%')) OR LOWER(a.codeSAP) LIKE LOWER(CONCAT('%', :term, '%'))")
    Page<ArticlePDR> search(@Param("term") String term, Pageable pageable);
    
    List<ArticlePDR> findByGroupeHomogene(GroupeHomogene groupeHomogene);
    
    @Query("SELECT a FROM ArticlePDR a WHERE a.seuilMin IS NOT NULL AND a.seuilMax IS NOT NULL")
    List<ArticlePDR> findAllWithSeuils();

       @Query("SELECT DISTINCT a FROM ArticlePDR a LEFT JOIN FETCH a.stocks")
       List<ArticlePDR> findAllWithStocks();

    @Query("SELECT DISTINCT a FROM ArticlePDR a LEFT JOIN FETCH a.consommations")
    List<ArticlePDR> findAllForAnalysis();

    long countByGroupeHomogene(GroupeHomogene groupeHomogene);

    @Query("SELECT COUNT(a) FROM ArticlePDR a " +
           "WHERE COALESCE((SELECT SUM(s.quantiteStock) FROM Stock s WHERE s.article = a), 0) = 0")
    long countArticlesWithZeroStock();
}


