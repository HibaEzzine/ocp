package com.ocp.pdr.mapper;

import org.springframework.stereotype.Component;

import com.ocp.pdr.dto.DemandeAchatDto;
import com.ocp.pdr.model.DemandeAchat;
import com.ocp.pdr.repository.UtilisateurRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DemandeAchatMapper {

    private final UtilisateurRepository utilisateurRepository;

    public DemandeAchatDto toDto(DemandeAchat entity) {
        if (entity == null) {
            return null;
        }

        return DemandeAchatDto.builder()
                .id(entity.getId())
                .numeroDA(entity.getNumeroDA())
                .numeroDossier(entity.getNumeroDossier())
                .numero(entity.getNumero())
                .codeOracle(entity.getCodeOracle())
                .codeSAP(entity.getCodeSAP())
                .description(entity.getDescription())
                .uniteDeMesure(entity.getUniteDeMesure())
                .quantite(entity.getQuantite())
                .secteur(entity.getSecteur())
                .commande(entity.getCommande())
                .fournisseur(entity.getFournisseur())
                .pourcentageLivraison(entity.getPourcentageLivraison())
                .delaiLivraison(entity.getDelaiLivraison())
                .dateNotification(entity.getDateNotification())
                .datePrevisionnelle(entity.getDatePrevisionnelle())
                .statut(entity.getStatut())
                .responsable(entity.getResponsable())
                .utilisateurId(entity.getUtilisateur() != null ? entity.getUtilisateur().getId() : null)
                .commentaire(entity.getCommentaire())
                .dateCreation(entity.getDateCreation())
                .dateModification(entity.getDateModification())
                .build();
    }

    public DemandeAchat toEntity(DemandeAchatDto dto) {
        if (dto == null) {
            return null;
        }

        DemandeAchat entity = DemandeAchat.builder()
                .id(dto.getId())
                .numeroDA(dto.getNumeroDA())
                .numeroDossier(dto.getNumeroDossier())
                .numero(dto.getNumero())
                .codeOracle(dto.getCodeOracle())
                .codeSAP(dto.getCodeSAP())
                .description(dto.getDescription())
                .uniteDeMesure(dto.getUniteDeMesure())
                .quantite(dto.getQuantite())
                .secteur(dto.getSecteur())
                .commande(dto.getCommande())
                .fournisseur(dto.getFournisseur())
                .pourcentageLivraison(dto.getPourcentageLivraison())
                .delaiLivraison(dto.getDelaiLivraison())
                .dateNotification(dto.getDateNotification())
                .datePrevisionnelle(dto.getDatePrevisionnelle())
                .statut(dto.getStatut())
                .responsable(dto.getResponsable())
                .commentaire(dto.getCommentaire())
                .dateCreation(dto.getDateCreation())
                .dateModification(dto.getDateModification())
                .build();

        if (dto.getUtilisateurId() != null) {
            utilisateurRepository.findById(dto.getUtilisateurId()).ifPresent(entity::setUtilisateur);
        }

        return entity;
    }
}
