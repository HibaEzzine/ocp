package com.ocp.pdr.model;

import com.ocp.pdr.model.enums.ModeApprovisionnement;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "resultat_approvisionnement")
public class ResultatApprovisionnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode")
    private ModeApprovisionnement mode;

    @Column(name = "quantite_a_lancer")
    private Double quantiteALancer;

    @Column(name = "priorite")
    private Integer priorite;

    @Column(name = "justification", columnDefinition = "TEXT")
    private String justification;

    @Column(name = "date_analyse")
    private LocalDate dateAnalyse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticlePDR article;

    public ResultatApprovisionnement() {}

    public ResultatApprovisionnement(Long id, ModeApprovisionnement mode, Double quantiteALancer, Integer priorite, String justification, LocalDate dateAnalyse, ArticlePDR article) {
        this.id = id;
        this.mode = mode;
        this.quantiteALancer = quantiteALancer;
        this.priorite = priorite;
        this.justification = justification;
        this.dateAnalyse = dateAnalyse;
        this.article = article;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ModeApprovisionnement getMode() {
        return mode;
    }

    public void setMode(ModeApprovisionnement mode) {
        this.mode = mode;
    }

    public Double getQuantiteALancer() {
        return quantiteALancer;
    }

    public void setQuantiteALancer(Double quantiteALancer) {
        this.quantiteALancer = quantiteALancer;
    }

    public Integer getPriorite() {
        return priorite;
    }

    public void setPriorite(Integer priorite) {
        this.priorite = priorite;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public LocalDate getDateAnalyse() {
        return dateAnalyse;
    }

    public void setDateAnalyse(LocalDate dateAnalyse) {
        this.dateAnalyse = dateAnalyse;
    }

    public ArticlePDR getArticle() {
        return article;
    }

    public void setArticle(ArticlePDR article) {
        this.article = article;
    }

    public String getCodeSAP() {
        return article != null ? article.getCodeSAP() : null;
    }

    public String getDesignation() {
        return article != null ? article.getDescription() : null;
    }
}
