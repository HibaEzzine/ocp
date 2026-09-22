package com.ocp.pdr.mapper;

import org.springframework.stereotype.Component;

import com.ocp.pdr.dto.UtilisateurDto;
import com.ocp.pdr.model.Utilisateur;

@Component
public class UtilisateurMapper {

    public UtilisateurDto toDto(Utilisateur entity) {
        if (entity == null) {
            return null;
        }

        return UtilisateurDto.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .prenom(entity.getPrenom())
                .email(entity.getEmail())
                .role(entity.getRole())
                .actif(entity.getActif())
                .build();
    }

    public Utilisateur toEntity(UtilisateurDto dto) {
        if (dto == null) {
            return null;
        }

        return Utilisateur.builder()
                .id(dto.getId())
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .email(dto.getEmail())
                .motDePasse(dto.getMotDePasse())
                .role(dto.getRole())
                .actif(dto.getActif() != null ? dto.getActif() : true)
                .build();
    }
}
