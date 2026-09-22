package com.ocp.pdr.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.ocp.pdr.model.ImportExcelDA;
import com.ocp.pdr.repository.ImportExcelDARepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImportExcelDAService {

    private final ImportExcelDARepository repo;

    public List<ImportExcelDA> findAll() {
        return repo.findAll();
    }

    public Optional<ImportExcelDA> findById(Long id) {
        return repo.findById(id);
    }

    public ImportExcelDA save(ImportExcelDA e) {
        return repo.save(e);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }
}
