package com.ocp.pdr.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalieConsommationIaDto {
    private String codeSAP;
    private String description;
    private Double consommationRecente;
    private Double moyenneHistorique;
    private Double ecartType;
    private Double seuil;
    private Double augmentationPourcentage;
    private String niveau;
    private Boolean anomalie;
    private String justification;
}
