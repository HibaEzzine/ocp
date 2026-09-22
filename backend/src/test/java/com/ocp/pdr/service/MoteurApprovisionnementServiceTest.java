package com.ocp.pdr.service;

import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.ResultatApprovisionnement;
import com.ocp.pdr.model.Stock;
import com.ocp.pdr.model.enums.GroupeHomogene;
import com.ocp.pdr.model.enums.ModeApprovisionnement;
import com.ocp.pdr.repository.AnomalieConsommationRepository;
import com.ocp.pdr.repository.ArticlePDRRepository;
import com.ocp.pdr.repository.ResultatApprovisionnementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests MoteurApprovisionnementService")
class MoteurApprovisionnementServiceTest {

    @Mock
    private RegleMinMaxService regleMinMaxService;

    @Mock
    private ReglePlanifieService reglePlanifieService;

    @Mock
    private RegleSurDemandeService regleSurDemandeService;

    @Mock
    private AnalyseConsommationService analyseConsommationService;

    @Mock
    private ArticlePDRRepository articlePDRRepository;

    @Mock
    private ResultatApprovisionnementRepository resultatApprovisionnementRepository;

    @Mock
    private AnomalieConsommationRepository anomalieConsommationRepository;

    @InjectMocks
    private MoteurApprovisionnementService moteurService;

    private ArticlePDR article;
    private Stock stock;

    @BeforeEach
    void setUp() {
        article = new ArticlePDR();
        article.setId(1L);
        article.setCodeSAP("SAP-001");
        article.setDescription("Pompe hydraulique");
        article.setGroupeHomogene(GroupeHomogene.CRITIQUE);
        article.setSeuilMin(10.0);
        article.setSeuilMax(50.0);
        article.setQuantiteInstallee(5.0);

        stock = new Stock();
        stock.setQuantiteStock(5.0);
        article.setStocks(new ArrayList<>(Arrays.asList(stock)));
    }

    @Test
    @DisplayName("Analyse article avec règle Min & Max")
    void testAnalyserArticleMinMax() {
        // Given
        when(regleMinMaxService.verifier(article)).thenReturn(true);
        when(regleMinMaxService.calculerQuantite(article)).thenReturn(45.0);
        when(resultatApprovisionnementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ResultatApprovisionnement resultat = moteurService.analyser(article);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getMode()).isEqualTo(ModeApprovisionnement.MIN_MAX);
        assertThat(resultat.getPriorite()).isEqualTo(1);
        assertThat(resultat.getQuantiteALancer()).isEqualTo(45.0);
        verify(regleMinMaxService).verifier(article);
        verify(regleMinMaxService).calculerQuantite(article);
    }

    @Test
    @DisplayName("Analyse article avec mode Planifié")
    void testAnalyserArticlePlanifie() {
        // Given
        when(regleMinMaxService.verifier(article)).thenReturn(false);
        when(reglePlanifieService.verifier(article)).thenReturn(true);
        when(reglePlanifieService.calculerQuantite(article)).thenReturn(20.0);
        when(resultatApprovisionnementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ResultatApprovisionnement resultat = moteurService.analyser(article);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getMode()).isEqualTo(ModeApprovisionnement.PLANIFIE);
        assertThat(resultat.getPriorite()).isEqualTo(2);
        assertThat(resultat.getQuantiteALancer()).isEqualTo(20.0);
    }

    @Test
    @DisplayName("Analyse article avec mode Sur Demande")
    void testAnalyserArticleSurDemande() {
        // Given
        when(regleMinMaxService.verifier(article)).thenReturn(false);
        when(reglePlanifieService.verifier(article)).thenReturn(false);
        when(regleSurDemandeService.verifier(article)).thenReturn(true);
        when(regleSurDemandeService.calculerQuantite(article)).thenReturn(15.0);
        when(resultatApprovisionnementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ResultatApprovisionnement resultat = moteurService.analyser(article);

        // Then
        assertThat(resultat).isNotNull();
        assertThat(resultat.getMode()).isEqualTo(ModeApprovisionnement.SUR_DEMANDE);
        assertThat(resultat.getPriorite()).isEqualTo(3);
        assertThat(resultat.getQuantiteALancer()).isEqualTo(15.0);
    }

    @Test
    @DisplayName("Analyse article sans besoin d'approvisionnement")
    void testAnalyserArticleSansBesoin() {
        // Given
        when(regleMinMaxService.verifier(article)).thenReturn(false);
        when(reglePlanifieService.verifier(article)).thenReturn(false);
        when(regleSurDemandeService.verifier(article)).thenReturn(false);

        // When
        ResultatApprovisionnement resultat = moteurService.analyser(article);

        // Then
        assertThat(resultat).isNull();
        verify(resultatApprovisionnementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Exécute analyse globale pour tous les articles")
    void testExecuterAnalyseGlobale() {
        // Given
        List<ArticlePDR> articles = Arrays.asList(article);
        when(articlePDRRepository.findAll()).thenReturn(articles);
        when(regleMinMaxService.verifier(article)).thenReturn(true);
        when(regleMinMaxService.calculerQuantite(article)).thenReturn(45.0);
        when(resultatApprovisionnementRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<ResultatApprovisionnement> resultats = moteurService.executerAnalyseGlobale();

        // Then
        assertThat(resultats).isNotEmpty();
        verify(resultatApprovisionnementRepository).deleteAll();
        verify(anomalieConsommationRepository).deleteAll();
        verify(analyseConsommationService).detecterEtSauvegarderAnomalie(article);
    }

    @Test
    @DisplayName("Obtient le summary de l'analyse")
    void testObtenirSummaryAnalyse() {
        // Given
        List<ResultatApprovisionnement> resultats = new ArrayList<>();
        ResultatApprovisionnement r1 = new ResultatApprovisionnement();
        r1.setMode(ModeApprovisionnement.MIN_MAX);
        resultats.add(r1);

        when(articlePDRRepository.count()).thenReturn(10L);
        when(resultatApprovisionnementRepository.findAll()).thenReturn(resultats);
        when(anomalieConsommationRepository.count()).thenReturn(2L);

        // When
        Map<String, Object> summary = moteurService.obtenirSummaryAnalyse();

        // Then
        assertThat(summary).isNotNull();
        assertThat(summary.get("totalArticles")).isEqualTo(10L);
        assertThat(summary.get("minMaxCount")).isEqualTo(1L);
        assertThat(summary.get("anomaliesCount")).isEqualTo(2L);
    }

    @Test
    @DisplayName("Gère les erreurs lors de l'analyse d'un article")
    void testAnalysisWithErrorHandling() {
        // Given - Une erreur qui est lancée au deleteAll
        doThrow(new RuntimeException("Erreur test")).when(resultatApprovisionnementRepository).deleteAll();

        // When & Then
        assertThatThrownBy(() -> moteurService.executerAnalyseGlobale())
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Erreur lors de l'analyse globale: Erreur test");
    }
}
