package com.ocp.pdr.dto;

import java.time.LocalDateTime;

public class ImportExcelDADto {
    private Long id;
    private String nomFichier;
    private LocalDateTime dateImport;
    private Integer nombreLignes;
    private String statutImport;

    public ImportExcelDADto() {}

    public ImportExcelDADto(Long id, String nomFichier, LocalDateTime dateImport, Integer nombreLignes, String statutImport) {
        this.id = id;
        this.nomFichier = nomFichier;
        this.dateImport = dateImport;
        this.nombreLignes = nombreLignes;
        this.statutImport = statutImport;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String nomFichier;
        private LocalDateTime dateImport;
        private Integer nombreLignes;
        private String statutImport;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder nomFichier(String nomFichier) { this.nomFichier = nomFichier; return this; }
        public Builder dateImport(LocalDateTime dateImport) { this.dateImport = dateImport; return this; }
        public Builder nombreLignes(Integer nombreLignes) { this.nombreLignes = nombreLignes; return this; }
        public Builder statutImport(String statutImport) { this.statutImport = statutImport; return this; }

        public ImportExcelDADto build() {
            return new ImportExcelDADto(id, nomFichier, dateImport, nombreLignes, statutImport);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public LocalDateTime getDateImport() { return dateImport; }
    public void setDateImport(LocalDateTime dateImport) { this.dateImport = dateImport; }

    public Integer getNombreLignes() { return nombreLignes; }
    public void setNombreLignes(Integer nombreLignes) { this.nombreLignes = nombreLignes; }

    public String getStatutImport() { return statutImport; }
    public void setStatutImport(String statutImport) { this.statutImport = statutImport; }
}
