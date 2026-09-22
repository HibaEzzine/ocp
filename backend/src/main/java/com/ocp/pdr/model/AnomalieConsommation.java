package com.ocp.pdr.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "anomalie_consommation")
public class AnomalieConsommation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consommation_mensuelle")
    private Double consommationMensuelle;

    @Column(name = "quantite_installee")
    private Double quantiteInstallee;

    @Column(name = "taux_consommation")
    private Double tauxConsommation;

    @Column(name = "seuil")
    private Double seuil;

    @Column(name = "date_detection")
    private LocalDate dateDetection;

    @Column(name = "statut")
    private String statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticlePDR article;

    public AnomalieConsommation() {}

    public AnomalieConsommation(Long id, Double consommationMensuelle, Double quantiteInstallee, Double tauxConsommation, Double seuil, LocalDate dateDetection, String statut, ArticlePDR article) {
        this.id = id;
        this.consommationMensuelle = consommationMensuelle;
        this.quantiteInstallee = quantiteInstallee;
        this.tauxConsommation = tauxConsommation;
        this.seuil = seuil;
        this.dateDetection = dateDetection;
        this.statut = statut;
        this.article = article;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getConsommationMensuelle() {
        return consommationMensuelle;
    }

    public void setConsommationMensuelle(Double consommationMensuelle) {
        this.consommationMensuelle = consommationMensuelle;
    }

    public Double getQuantiteInstallee() {
        return quantiteInstallee;
    }

    public void setQuantiteInstallee(Double quantiteInstallee) {
        this.quantiteInstallee = quantiteInstallee;
    }

    public Double getTauxConsommation() {
        return tauxConsommation;
    }

    public void setTauxConsommation(Double tauxConsommation) {
        this.tauxConsommation = tauxConsommation;
    }

    public Double getSeuil() {
        return seuil;
    }

    public void setSeuil(Double seuil) {
        this.seuil = seuil;
    }

    public LocalDate getDateDetection() {
        return dateDetection;
    }

    public void setDateDetection(LocalDate dateDetection) {
        this.dateDetection = dateDetection;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public ArticlePDR getArticle() {
        return article;
    }

    public void setArticle(ArticlePDR article) {
        this.article = article;
    }
}
