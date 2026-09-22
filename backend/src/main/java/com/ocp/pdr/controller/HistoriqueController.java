package com.ocp.pdr.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ocp.pdr.dto.HistoriqueDADto;
import com.ocp.pdr.mapper.HistoriqueDAMapper;
import com.ocp.pdr.service.HistoriqueDAService;

import lombok.RequiredArgsConstructor;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping({"/api/historiques", "/api/historique"})
@RequiredArgsConstructor
public class HistoriqueController {

    private final HistoriqueDAService service;
    private final HistoriqueDAMapper mapper;

    @GetMapping
    public List<HistoriqueDADto> getAll() {
        return service.findAll().stream().map(mapper::toDto).toList();
    }

    @GetMapping("/demande/{demandeId}")
    public List<HistoriqueDADto> getByDemandeId(@PathVariable Long demandeId) {
        return service.findByDemandeAchatId(demandeId).stream().map(mapper::toDto).toList();
    }
}
