package com.ocp.pdr.mapper;

import org.springframework.stereotype.Component;

import com.ocp.pdr.dto.ImportExcelDADto;
import com.ocp.pdr.model.ImportExcelDA;

@Component
public class ImportExcelDAMapper {

    public ImportExcelDADto toDto(ImportExcelDA entity) {
        if (entity == null) {
            return null;
        }

        return ImportExcelDADto.builder()
                .id(entity.getId())
                .nomFichier(entity.getNomFichier())
                .dateImport(entity.getDateImport())
                .nombreLignes(entity.getTotalLignes())
                .statutImport(entity.getStatut())
                .build();
    }

    public ImportExcelDA toEntity(ImportExcelDADto dto) {
        if (dto == null) {
            return null;
        }

        return ImportExcelDA.builder()
                .id(dto.getId())
                .nomFichier(dto.getNomFichier())
                .dateImport(dto.getDateImport())
                .totalLignes(dto.getNombreLignes())
                .statut(dto.getStatutImport())
                .build();
    }
}
