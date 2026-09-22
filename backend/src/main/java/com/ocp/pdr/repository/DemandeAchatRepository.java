package com.ocp.pdr.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ocp.pdr.model.DemandeAchat;

@Repository
public interface DemandeAchatRepository extends JpaRepository<DemandeAchat, Long> {
}
