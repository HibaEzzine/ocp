package com.ocp.pdr.repository;

import com.ocp.pdr.model.BacklogOT;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BacklogOTRepository extends JpaRepository<BacklogOT, Long> {
}
