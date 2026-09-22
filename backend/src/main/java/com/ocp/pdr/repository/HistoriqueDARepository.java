package com.ocp.pdr.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ocp.pdr.model.HistoriqueDA;

@Repository
public interface HistoriqueDARepository extends JpaRepository<HistoriqueDA, Long> {
    List<HistoriqueDA> findAllByOrderByDateActionDesc();

    List<HistoriqueDA> findByDemandeAchatIdOrderByDateActionDesc(Long demandeAchatId);
}
