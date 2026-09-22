package com.ocp.pdr.service;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.ocp.pdr.model.ArticlePDR;
import com.ocp.pdr.model.BacklogOT;
import com.ocp.pdr.model.Stock;
import com.ocp.pdr.model.enums.GroupeHomogene;

class ReglesApprovisionnementTest {

    private final RegleMinMaxService minMaxService = new RegleMinMaxService();
    private final ReglePlanifieService planifieService = new ReglePlanifieService();
    private final RegleSurDemandeService surDemandeService = new RegleSurDemandeService();

    @Test
    void priorite1_minMax_pour_article_critique_en_sous_stock() {
        ArticlePDR article = articleAvecStockEtGroupe(20.0, 40.0, GroupeHomogene.CRITIQUE);

        assertTrue(minMaxService.verifier(article));
        assertFalse(planifieService.verifier(article));
        assertFalse(surDemandeService.verifier(article));
    }

    @Test
    void priorite2_planifie_pour_article_dans_backlog_sans_groupe_prioritaire() {
        ArticlePDR article = articleAvecStockEtGroupe(20.0, 40.0, null);
        article.setBacklogOTs(List.of(ot(5.0)));

        assertFalse(minMaxService.verifier(article));
        assertTrue(planifieService.verifier(article));
        assertFalse(surDemandeService.verifier(article));
    }

    @Test
    void surDemande_estRefuseeDesQuUneLigneExisteDansLeBacklog() {
        ArticlePDR article = articleAvecStockEtGroupe(20.0, 40.0, null);
        article.setBacklogOTs(List.of(ot(0.0)));

        assertFalse(surDemandeService.verifier(article));
    }

    @Test
    void priorite3_surDemande_si_non_couvert_non_backlog_et_non_prioritaire() {
        ArticlePDR article = articleAvecStockEtGroupe(20.0, 40.0, null);

        assertFalse(minMaxService.verifier(article));
        assertFalse(planifieService.verifier(article));
        assertTrue(surDemandeService.verifier(article));
    }

    @Test
    void surDemande_utiliseLeDeficitQuandAucunBesoinEnCoursNExiste() {
        ArticlePDR article = articleAvecStockEtGroupe(20.0, 40.0, null);
        article.setSeuilMax(50.0);

        assertEquals(30.0, surDemandeService.calculerQuantite(article));
    }

    @Test
    void planifie_utiliseLeSeuilMaxMoinsLeStock() {
        ArticlePDR article = articleAvecStockEtGroupe(20.0, 40.0, null);
        article.setSeuilMax(50.0);
        article.setBacklogOTs(List.of(ot(15.0)));

        assertEquals(30.0, planifieService.calculerQuantite(article));
    }

    @Test
    void un_article_prioritaire_ne_devient_pas_planifie_meme_si_backlog_existe() {
        ArticlePDR article = articleAvecStockEtGroupe(20.0, 40.0, GroupeHomogene.CURATIF);
        article.setBacklogOTs(List.of(ot(4.0)));

        assertTrue(minMaxService.verifier(article));
        assertFalse(planifieService.verifier(article));
    }

    private ArticlePDR articleAvecStockEtGroupe(Double stock, Double seuilMin, GroupeHomogene groupeHomogene) {
        ArticlePDR article = new ArticlePDR();
        article.setGroupeHomogene(groupeHomogene);
        article.setSeuilMin(seuilMin);
        article.setStocks(List.of(stockArticle(stock)));
        article.setBacklogOTs(List.of());
        return article;
    }

    private Stock stockArticle(Double quantite) {
        Stock stock = new Stock();
        stock.setQuantiteStock(quantite);
        return stock;
    }

    private BacklogOT ot(Double quantiteNonLancee) {
        BacklogOT ot = new BacklogOT();
        ot.setQuantiteNonLancee(quantiteNonLancee);
        return ot;
    }
}
