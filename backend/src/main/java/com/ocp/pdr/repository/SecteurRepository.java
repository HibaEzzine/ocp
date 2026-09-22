package com.ocp.pdr.repository;

import com.ocp.pdr.model.Secteur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecteurRepository extends JpaRepository<Secteur, Long> {
    Optional<Secteur> findByNom(String nom);
}
