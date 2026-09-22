package com.ocp.pdr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.ocp.pdr.dto.response.PredictionIaResponse;
import com.ocp.pdr.dto.response.PredictionIaResultDto;
import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.Consommation;
import com.ocp.pdr.repository.ArticlePDRRepository;

@ExtendWith(MockitoExtension.class)
class IaPredictionServiceTest {

    @Mock
    private ArticlePDRRepository articlePDRRepository;

    private IaPredictionService iaPredictionService;
    private HttpServer httpServer;

    @BeforeEach
    void setUp() throws Exception {
        iaPredictionService = new IaPredictionService(articlePDRRepository);
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/predict", exchange -> {
            String json = """
                {
                  "predictions": [
                    {
                      "codeSAP": "SAP-1",
                      "description": "Article 1",
                      "quantiteInstallee": 10.0,
                      "consommationActuelle": 4.0,
                      "consommationPredite": 5.0,
                      "seuil": 20.0,
                      "ratio": 0.25,
                      "scoreRisque": 20,
                      "niveauRisque": "FAIBLE",
                      "alerte": false,
                      "ratioRisque": 25.0,
                      "statut": "PREDICTION_DISPONIBLE",
                      "message": "OK",
                      "explications": ["stable"],
                      "features": {"tendance": "STABLE"},
                      "historique": [],
                      "nbMoisHistorique": 3,
                      "modeleUtilise": "PYTHON_RANDOM_FOREST"
                    },
                    {
                      "codeSAP": "SAP-2",
                      "description": "Article 2",
                      "quantiteInstallee": 5.0,
                      "consommationActuelle": 15.0,
                      "consommationPredite": 18.0,
                      "seuil": 10.0,
                      "ratio": 1.8,
                      "scoreRisque": 75,
                      "niveauRisque": "ELEVE",
                      "alerte": true,
                      "ratioRisque": 180.0,
                      "statut": "PREDICTION_DISPONIBLE",
                      "message": "Alerte",
                      "explications": ["alerte"],
                      "features": {"tendance": "HAUSSE"},
                      "historique": [],
                      "nbMoisHistorique": 3,
                      "modeleUtilise": "PYTHON_RANDOM_FOREST"
                    }
                  ]
                }
                """;
            byte[] payload = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, payload.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(payload);
            }
        });
        httpServer.start();
        ReflectionTestUtils.setField(iaPredictionService, "aiServiceUrl", "http://localhost:" + httpServer.getAddress().getPort());
    }

    @AfterEach
    void tearDown() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    private Consommation creerConso(double quantite, LocalDate date) {
        Consommation c = new Consommation();
        c.setQuantiteConsommee(quantite);
        c.setDateConsommation(date);
        return c;
    }

    @Test
    @DisplayName("Prédiction normale : Faible risque, pas d'alerte")
    void testPredictionNormaleFaibleRisque() {
        ArticlePDR article = new ArticlePDR();
        article.setCodeSAP("SAP-001");
        article.setDescription("Roulement standard");
        article.setQuantiteInstallee(10.0); // Seuil = 20.0

        List<Consommation> consos = List.of(
            creerConso(4.0, LocalDate.of(2024, 1, 10)),
            creerConso(4.0, LocalDate.of(2024, 2, 10)),
            creerConso(4.5, LocalDate.of(2024, 3, 10)),
            creerConso(4.0, LocalDate.of(2024, 4, 10))
        );
        article.setConsommations(consos);

        List<PredictionIaResultDto> resultats = iaPredictionService.executerModeleLocal(List.of(article));
        assertThat(resultats).hasSize(1);

        PredictionIaResultDto dto = resultats.get(0);
        assertThat(dto.getNiveauRisque()).isEqualTo("FAIBLE");
        assertThat(dto.getAlerte()).isFalse();
        assertThat(dto.getSeuil()).isEqualTo(20.0);
        assertThat(dto.getScoreRisque()).isLessThanOrEqualTo(39);
        assertThat(dto.getExplications()).isNotEmpty();
        assertThat(dto.getFeatures()).isNotNull();
    }

    @Test
    @DisplayName("Prédiction en très forte hausse : Risque Élevé / Critique et alerte déclenchée")
    void testPredictionInhabituelleRisqueEleve() {
        ArticlePDR article = new ArticlePDR();
        article.setCodeSAP("SAP-002");
        article.setDescription("Filtre haute pression");
        article.setQuantiteInstallee(10.0); // Seuil = 20.0

        List<Consommation> consos = List.of(
            creerConso(10.0, LocalDate.of(2024, 1, 10)),
            creerConso(16.0, LocalDate.of(2024, 2, 10)),
            creerConso(22.0, LocalDate.of(2024, 3, 10)),
            creerConso(30.0, LocalDate.of(2024, 4, 10))
        );
        article.setConsommations(consos);

        List<PredictionIaResultDto> resultats = iaPredictionService.executerModeleLocal(List.of(article));
        assertThat(resultats).hasSize(1);

        PredictionIaResultDto dto = resultats.get(0);
        assertThat(dto.getAlerte()).isTrue();
        assertThat(dto.getNiveauRisque()).isIn("ELEVE", "CRITIQUE");
        assertThat(dto.getScoreRisque()).isGreaterThanOrEqualTo(70);
        assertThat(dto.getConsommationPredite()).isGreaterThan(20.0);
    }

    @Test
    @DisplayName("Prédiction proche du seuil : Risque Moyen")
    void testPredictionProcheDuSeuilRisqueMoyen() {
        ArticlePDR article = new ArticlePDR();
        article.setCodeSAP("SAP-003");
        article.setDescription("Joint torique");
        article.setQuantiteInstallee(10.0); // Seuil = 20.0

        List<Consommation> consos = List.of(
            creerConso(12.0, LocalDate.of(2024, 1, 10)),
            creerConso(13.0, LocalDate.of(2024, 2, 10)),
            creerConso(14.0, LocalDate.of(2024, 3, 10))
        );
        article.setConsommations(consos);

        List<PredictionIaResultDto> resultats = iaPredictionService.executerModeleLocal(List.of(article));
        assertThat(resultats).hasSize(1);

        PredictionIaResultDto dto = resultats.get(0);
        assertThat(dto.getNiveauRisque()).isEqualTo("MOYEN");
        assertThat(dto.getScoreRisque()).isBetween(40, 69);
    }

    @Test
    @DisplayName("Historique insuffisant (< 3 mois) : statut INSUFFISANT et pas de fausse prédiction")
    void testHistoriqueInsuffisantMoinsDe3Mois() {
        ArticlePDR article = new ArticlePDR();
        article.setCodeSAP("SAP-004");
        article.setDescription("Vanne spéciale");
        article.setQuantiteInstallee(5.0);

        List<Consommation> consos = List.of(
            creerConso(10.0, LocalDate.of(2024, 1, 10)),
            creerConso(12.0, LocalDate.of(2024, 2, 10))
        );
        article.setConsommations(consos);

        List<PredictionIaResultDto> resultats = iaPredictionService.executerModeleLocal(List.of(article));
        assertThat(resultats).hasSize(1);

        PredictionIaResultDto dto = resultats.get(0);
        assertThat(dto.getStatut()).isEqualTo("INSUFFISANT");
        assertThat(dto.getNiveauRisque()).isEqualTo("NON_EVALUABLE");
        assertThat(dto.getConsommationPredite()).isNull();
        assertThat(dto.getScoreRisque()).isEqualTo(0);
        assertThat(dto.getAlerte()).isFalse();
    }

    @Test
    @DisplayName("Quantité installée nulle : statut SANS_QUANTITE_INSTALLEE")
    void testQuantiteInstalleeNulle() {
        ArticlePDR article = new ArticlePDR();
        article.setCodeSAP("SAP-005");
        article.setDescription("Courroie");
        article.setQuantiteInstallee(0.0);

        List<Consommation> consos = List.of(
            creerConso(10.0, LocalDate.of(2024, 1, 10)),
            creerConso(12.0, LocalDate.of(2024, 2, 10)),
            creerConso(14.0, LocalDate.of(2024, 3, 10))
        );
        article.setConsommations(consos);

        List<PredictionIaResultDto> resultats = iaPredictionService.executerModeleLocal(List.of(article));
        assertThat(resultats).hasSize(1);

        PredictionIaResultDto dto = resultats.get(0);
        assertThat(dto.getStatut()).isEqualTo("SANS_QUANTITE_INSTALLEE");
        assertThat(dto.getNiveauRisque()).isEqualTo("NON_EVALUABLE");
        assertThat(dto.getAlerte()).isFalse();
    }

    @Test
    @DisplayName("Génération globale avec le service Python actif")
    void testGenererPredictionsGlobal() {
        ArticlePDR art1 = new ArticlePDR();
        art1.setCodeSAP("SAP-1");
        art1.setQuantiteInstallee(10.0);
        art1.setConsommations(List.of(
            creerConso(4.0, LocalDate.of(2024, 1, 1)),
            creerConso(4.0, LocalDate.of(2024, 2, 1)),
            creerConso(4.0, LocalDate.of(2024, 3, 1))
        ));

        ArticlePDR art2 = new ArticlePDR();
        art2.setCodeSAP("SAP-2");
        art2.setQuantiteInstallee(5.0);
        art2.setConsommations(List.of(
            creerConso(12.0, LocalDate.of(2024, 1, 1)),
            creerConso(15.0, LocalDate.of(2024, 2, 1)),
            creerConso(20.0, LocalDate.of(2024, 3, 1))
        ));

        when(articlePDRRepository.findAllForAnalysis()).thenReturn(List.of(art1, art2));

        PredictionIaResponse response = iaPredictionService.genererPredictions();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getTotalArticles()).isEqualTo(2);
        assertThat(response.getNbAlertes()).isGreaterThanOrEqualTo(1);
        assertThat(response.getPredictions()).hasSize(2);
    }

    @Test
    @DisplayName("Service IA indisponible : exception explicite sans fallback Java")
    void testGenererPredictionsSansServicePython() {
        ArticlePDR art1 = new ArticlePDR();
        art1.setCodeSAP("SAP-1");
        art1.setQuantiteInstallee(10.0);
        art1.setConsommations(List.of(
            creerConso(4.0, LocalDate.of(2024, 1, 1)),
            creerConso(4.0, LocalDate.of(2024, 2, 1)),
            creerConso(4.0, LocalDate.of(2024, 3, 1))
        ));
        when(articlePDRRepository.findAllForAnalysis()).thenReturn(List.of(art1));

        ReflectionTestUtils.setField(iaPredictionService, "aiServiceUrl", "http://localhost:1");

        assertThatThrownBy(iaPredictionService::genererPredictions)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Service IA Python indisponible");
    }
}
