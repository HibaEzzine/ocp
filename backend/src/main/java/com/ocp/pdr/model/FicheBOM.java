package com.ocp.pdr.model;

import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "bom")
public class FicheBOM {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference", columnDefinition = "LONGTEXT")
    private String reference;

    @Column(name = "quantite_par_equipement")
    private Double quantiteParEquipement;

    @Column(name = "date_import")
    private LocalDate dateImport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticlePDR article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secteur_id", nullable = false)
    private Secteur secteur;

    public FicheBOM() {}

    public FicheBOM(Long id, String reference, Double quantiteParEquipement, LocalDate dateImport, ArticlePDR article, Secteur secteur) {
        this.id = id;
        this.reference = reference;
        this.quantiteParEquipement = quantiteParEquipement;
        this.dateImport = dateImport;
        this.article = article;
        this.secteur = secteur;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Double getQuantiteParEquipement() {
        return quantiteParEquipement;
    }

    public void setQuantiteParEquipement(Double quantiteParEquipement) {
        this.quantiteParEquipement = quantiteParEquipement;
    }

    public LocalDate getDateImport() {
        return dateImport;
    }

    public void setDateImport(LocalDate dateImport) {
        this.dateImport = dateImport;
    }

    public ArticlePDR getArticle() {
        return article;
    }

    public void setArticle(ArticlePDR article) {
        this.article = article;
    }

    public Secteur getSecteur() {
        return secteur;
    }

    public void setSecteur(Secteur secteur) {
        this.secteur = secteur;
    }
}
