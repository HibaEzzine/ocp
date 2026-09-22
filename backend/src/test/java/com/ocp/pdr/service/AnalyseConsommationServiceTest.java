package com.ocp.pdr.service;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.Consommation;
import com.ocp.pdr.repository.AnomalieConsommationRepository;

@ExtendWith(MockitoExtension.class)
class AnalyseConsommationServiceTest {

    @Mock
    private AnomalieConsommationRepository anomalieRepository;

    private AnalyseConsommationService service;
    private ArticlePDR article;

    @BeforeEach
    void setUp() {
        service = new AnalyseConsommationService(anomalieRepository);
        article = new ArticlePDR();
        article.setQuantiteInstallee(10.0);
    }

    @Test
    void detecteUneConsommationStrictementSuperieureA200Pourcent() {
        article.setConsommations(List.of(consommation(21.0, LocalDate.now())));

        assertThat(service.verifierSeuil(article)).isTrue();
    }

    @Test
    void neDetectePasUneConsommationEgaleA200Pourcent() {
        article.setConsommations(List.of(consommation(20.0, LocalDate.now())));

        assertThat(service.verifierSeuil(article)).isFalse();
    }

    @Test
    void utiliseLeDernierMoisDisponibleEtIgnoreLesMoisAnterieurs() {
        article.setConsommations(List.of(
                consommation(100.0, LocalDate.of(2026, 6, 30)),
                consommation(21.0, LocalDate.of(2026, 7, 31))));

        assertThat(service.calculerConsommationMensuelle(article)).isEqualTo(21.0);
        assertThat(service.verifierSeuil(article)).isTrue();
    }

    @Test
    void refuseLaDetectionSansQuantiteInstallee() {
        article.setQuantiteInstallee(null);
        article.setConsommations(List.of(consommation(100.0, LocalDate.now())));

        assertThat(service.verifierSeuil(article)).isFalse();
        assertThat(service.calculerTauxConsommation(article)).isZero();
    }

    private Consommation consommation(double quantite, LocalDate date) {
        Consommation consommation = new Consommation();
        consommation.setQuantiteConsommee(quantite);
        consommation.setDateConsommation(date);
        return consommation;
    }
}
