package com.ocp.pdr.mapper;

import org.springframework.stereotype.Component;

import com.ocp.pdr.dto.HistoriqueDADto;
import com.ocp.pdr.model.HistoriqueDA;

@Component
public class HistoriqueDAMapper {

    public HistoriqueDADto toDto(HistoriqueDA entity) {
        if (entity == null) {
            return null;
        }

        String nomComplet = "";
        if (entity.getUtilisateur() != null) {
            nomComplet = (entity.getUtilisateur().getPrenom() != null ? entity.getUtilisateur().getPrenom() + " " : "")
                    + (entity.getUtilisateur().getNom() != null ? entity.getUtilisateur().getNom() : "");
        }

        return HistoriqueDADto.builder()
                .id(entity.getId())
                .action(entity.getAction())
                .ancienneValeur(entity.getAncienneValeur())
                .nouvelleValeur(entity.getNouvelleValeur())
                .dateAction(entity.getDateAction())
                .reportingId(entity.getDemandeAchat() != null ? entity.getDemandeAchat().getId() : null)
                .nomUtilisateur(nomComplet.trim())
                .build();
    }
}
