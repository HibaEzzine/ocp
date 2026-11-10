package com.ocp.pdr.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionAnomaliesIaResponse {
    private Boolean success;
    private Integer totalArticles;
    private Integer nbAnomalies;
    private Integer nbCritique;
    private Integer nbInhabituelle;
    private Integer nbNormal;
    private Integer nbNonEvaluable;
    private List<AnomalieConsommationIaDto> results;
    private String message;
    private Long timestamp;
}
