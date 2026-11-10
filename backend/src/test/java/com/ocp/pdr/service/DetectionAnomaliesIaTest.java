package com.ocp.pdr.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ocp.pdr.dto.response.AnomalieConsommationIaDto;
import com.ocp.pdr.dto.response.DetectionAnomaliesIaResponse;
import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.Consommation;

class DetectionAnomaliesIaTest {

    private ArticlePDR article(String codeSAP, String description, List<Consommation> consos) {
        ArticlePDR a = new ArticlePDR();
        a.setCodeSAP(codeSAP);
        a.setDescription(description);
        a.setConsommations(consos);
        return a;
    }

    private Consommation conso(int year, int month, int day, double qte) {
        Consommation c = new Consommation();
        c.setDateConsommation(LocalDate.of(year, month, day));
        c.setQuantiteConsommee(qte);
        return c;
    }

    @Test
    @DisplayName("Vérifie les calculs statistiques et la détection d'anomalies en mode repli local")
    void testDetectionAnomaliesLocale() {
        IaPredictionService service = new IaPredictionService(null);

        // Article 1 : Moins de 3 mois -> NON_EVALUABLE
        ArticlePDR art1 = article("ART_COURT", "Article récent", List.of(
                conso(2026, 1, 10, 5.0),
                conso(2026, 2, 10, 6.0)
        ));

        // Article 2 : Normal (consommations stables)
        ArticlePDR art2 = article("ART_NORMAL", "Article régulier", List.of(
                conso(2026, 1, 10, 10.0),
                conso(2026, 2, 10, 12.0),
                conso(2026, 3, 10, 11.0),
                conso(2026, 4, 10, 10.0),
                conso(2026, 5, 10, 11.0)
        ));

        // Article 3 : Inhabituelle (hausse au dernier mois)
        ArticlePDR art3 = article("80169568", "TAMBOUR", List.of(
                conso(2026, 1, 15, 2.0),
                conso(2026, 2, 15, 3.0),
                conso(2026, 3, 15, 2.0),
                conso(2026, 4, 15, 4.0),
                conso(2026, 5, 15, 5.0),
                conso(2026, 6, 15, 8.0),
                conso(2026, 7, 15, 12.0),
                conso(2026, 8, 15, 18.0)
        ));

        List<AnomalieConsommationIaDto> results = service.executerDetectionAnomaliesLocale(List.of(art1, art2, art3));
        assertNotNull(results);
        assertEquals(3, results.size());

        // Vérification article 1
        AnomalieConsommationIaDto dto1 = results.get(0);
        assertEquals("NON_EVALUABLE", dto1.getNiveau());
        assertFalse(dto1.getAnomalie());

        // Vérification article 2
        AnomalieConsommationIaDto dto2 = results.get(1);
        assertEquals("NORMAL", dto2.getNiveau());
        assertFalse(dto2.getAnomalie());

        // Vérification article 3
        AnomalieConsommationIaDto dto3 = results.get(2);
        assertEquals("80169568", dto3.getCodeSAP());
        assertEquals("INHABITUELLE", dto3.getNiveau());
        assertTrue(dto3.getAnomalie());
        assertEquals(18.0, dto3.getConsommationRecente());
        assertEquals(6.75, dto3.getMoyenneHistorique());
        assertTrue(dto3.getConsommationRecente() > dto3.getSeuil());
    }

    @Test
    @DisplayName("Vérifie la construction de la réponse globale DTO")
    void testResponseDto() {
        AnomalieConsommationIaDto item = AnomalieConsommationIaDto.builder()
                .codeSAP("TEST01")
                .description("Test")
                .consommationRecente(20.0)
                .moyenneHistorique(10.0)
                .ecartType(3.0)
                .seuil(16.0)
                .augmentationPourcentage(100.0)
                .niveau("INHABITUELLE")
                .anomalie(true)
                .justification("Dépassement du seuil")
                .build();

        DetectionAnomaliesIaResponse response = DetectionAnomaliesIaResponse.builder()
                .success(true)
                .totalArticles(1)
                .nbAnomalies(1)
                .nbCritique(0)
                .nbInhabituelle(1)
                .nbNormal(0)
                .nbNonEvaluable(0)
                .results(List.of(item))
                .message("Test OK")
                .timestamp(System.currentTimeMillis())
                .build();

        assertTrue(response.getSuccess());
        assertEquals(1, response.getTotalArticles());
        assertEquals(1, response.getNbAnomalies());
        assertEquals("INHABITUELLE", response.getResults().get(0).getNiveau());
    }
}
