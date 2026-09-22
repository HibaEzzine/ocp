package com.ocp.pdr.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "backlog_ot")
public class BacklogOT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_ot")
    private String numeroOT;

    @Column(name = "quantite_non_lancee")
    private Double quantiteNonLancee;

    @Column(name = "date_import")
    private LocalDate dateImport;

    @Column(name = "source_fichier")
    private String sourceFichier;

    @Column(name = "date_import_systeme")
    private LocalDateTime dateImportSysteme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private ArticlePDR article;

    public BacklogOT() {}

    public BacklogOT(Long id, String numeroOT, Double quantiteNonLancee, LocalDate dateImport, String sourceFichier, LocalDateTime dateImportSysteme, ArticlePDR article) {
        this.id = id;
        this.numeroOT = numeroOT;
        this.quantiteNonLancee = quantiteNonLancee;
        this.dateImport = dateImport;
        this.sourceFichier = sourceFichier;
        this.dateImportSysteme = dateImportSysteme;
        this.article = article;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroOT() {
        return numeroOT;
    }

    public void setNumeroOT(String numeroOT) {
        this.numeroOT = numeroOT;
    }

    public Double getQuantiteNonLancee() {
        return quantiteNonLancee;
    }

    public void setQuantiteNonLancee(Double quantiteNonLancee) {
        this.quantiteNonLancee = quantiteNonLancee;
    }

    public LocalDate getDateImport() {
        return dateImport;
    }

    public void setDateImport(LocalDate dateImport) {
        this.dateImport = dateImport;
    }

    public String getSourceFichier() {
        return sourceFichier;
    }

    public void setSourceFichier(String sourceFichier) {
        this.sourceFichier = sourceFichier;
    }

    public LocalDateTime getDateImportSysteme() {
        return dateImportSysteme;
    }

    public void setDateImportSysteme(LocalDateTime dateImportSysteme) {
        this.dateImportSysteme = dateImportSysteme;
    }

    public ArticlePDR getArticle() {
        return article;
    }

    public void setArticle(ArticlePDR article) {
        this.article = article;
    }
}
