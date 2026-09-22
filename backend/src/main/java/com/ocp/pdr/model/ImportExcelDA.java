package com.ocp.pdr.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "import_excel_da")
public class ImportExcelDA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_fichier")
    private String nomFichier;

    @Column(name = "date_import")
    private LocalDateTime dateImport;

    @Column(name = "total_lignes")
    private Integer totalLignes;

    @Column(name = "lignes_importees")
    private Integer lignesImportees;

    @Column(name = "lignes_en_erreur")
    private Integer lignesEnErreur;

    @Column(name = "statut")
    private String statut;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    public ImportExcelDA() {}

    public ImportExcelDA(Long id, String nomFichier, LocalDateTime dateImport, Integer totalLignes,
                         Integer lignesImportees, Integer lignesEnErreur, String statut, String message) {
        this.id = id;
        this.nomFichier = nomFichier;
        this.dateImport = dateImport;
        this.totalLignes = totalLignes;
        this.lignesImportees = lignesImportees;
        this.lignesEnErreur = lignesEnErreur;
        this.statut = statut;
        this.message = message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String nomFichier;
        private LocalDateTime dateImport;
        private Integer totalLignes;
        private Integer lignesImportees;
        private Integer lignesEnErreur;
        private String statut;
        private String message;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder nomFichier(String nomFichier) { this.nomFichier = nomFichier; return this; }
        public Builder dateImport(LocalDateTime dateImport) { this.dateImport = dateImport; return this; }
        public Builder totalLignes(Integer totalLignes) { this.totalLignes = totalLignes; return this; }
        public Builder lignesImportees(Integer lignesImportees) { this.lignesImportees = lignesImportees; return this; }
        public Builder lignesEnErreur(Integer lignesEnErreur) { this.lignesEnErreur = lignesEnErreur; return this; }
        public Builder statut(String statut) { this.statut = statut; return this; }
        public Builder message(String message) { this.message = message; return this; }

        public ImportExcelDA build() {
            return new ImportExcelDA(id, nomFichier, dateImport, totalLignes, lignesImportees, lignesEnErreur, statut, message);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public LocalDateTime getDateImport() { return dateImport; }
    public void setDateImport(LocalDateTime dateImport) { this.dateImport = dateImport; }

    public Integer getTotalLignes() { return totalLignes; }
    public void setTotalLignes(Integer totalLignes) { this.totalLignes = totalLignes; }

    public Integer getLignesImportees() { return lignesImportees; }
    public void setLignesImportees(Integer lignesImportees) { this.lignesImportees = lignesImportees; }

    public Integer getLignesEnErreur() { return lignesEnErreur; }
    public void setLignesEnErreur(Integer lignesEnErreur) { this.lignesEnErreur = lignesEnErreur; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
