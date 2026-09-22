package com.ocp.pdr.repository;

import com.ocp.pdr.model.BesoinEnCours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BesoinEnCoursRepository extends JpaRepository<BesoinEnCours, Long> {
}
