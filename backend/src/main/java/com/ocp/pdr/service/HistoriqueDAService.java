package com.ocp.pdr.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ocp.pdr.model.HistoriqueDA;
import com.ocp.pdr.repository.HistoriqueDARepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoriqueDAService {

    private final HistoriqueDARepository repo;

    public List<HistoriqueDA> findAll() {
        return repo.findAllByOrderByDateActionDesc();
    }

    public List<HistoriqueDA> findByDemandeAchatId(Long demandeAchatId) {
        return repo.findByDemandeAchatIdOrderByDateActionDesc(demandeAchatId);
    }
}
