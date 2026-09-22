package com.ocp.pdr.dto;

import java.time.LocalDateTime;

public class HistoriqueDADto {
    private Long id;
    private String action;
    private String ancienneValeur;
    private String nouvelleValeur;
    private LocalDateTime dateAction;
    private Long reportingId;
    private String nomUtilisateur;

    public HistoriqueDADto() {}

    public HistoriqueDADto(Long id, String action, String ancienneValeur, String nouvelleValeur,
                           LocalDateTime dateAction, Long reportingId, String nomUtilisateur) {
        this.id = id;
        this.action = action;
        this.ancienneValeur = ancienneValeur;
        this.nouvelleValeur = nouvelleValeur;
        this.dateAction = dateAction;
        this.reportingId = reportingId;
        this.nomUtilisateur = nomUtilisateur;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String action;
        private String ancienneValeur;
        private String nouvelleValeur;
        private LocalDateTime dateAction;
        private Long reportingId;
        private String nomUtilisateur;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder action(String action) { this.action = action; return this; }
        public Builder ancienneValeur(String ancienneValeur) { this.ancienneValeur = ancienneValeur; return this; }
        public Builder nouvelleValeur(String nouvelleValeur) { this.nouvelleValeur = nouvelleValeur; return this; }
        public Builder dateAction(LocalDateTime dateAction) { this.dateAction = dateAction; return this; }
        public Builder reportingId(Long reportingId) { this.reportingId = reportingId; return this; }
        public Builder nomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; return this; }

        public HistoriqueDADto build() {
            return new HistoriqueDADto(id, action, ancienneValeur, nouvelleValeur, dateAction, reportingId, nomUtilisateur);
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getAnancienneValeur() { return ancienneValeur; }
    public String getAncienneValeur() { return ancienneValeur; }
    public void setAncienneValeur(String ancienneValeur) { this.ancienneValeur = ancienneValeur; }

    public String getNouvelleValeur() { return nouvelleValeur; }
    public void setNouvelleValeur(String nouvelleValeur) { this.nouvelleValeur = nouvelleValeur; }

    public LocalDateTime getDateAction() { return dateAction; }
    public void setDateAction(LocalDateTime dateAction) { this.dateAction = dateAction; }

    public Long getReportingId() { return reportingId; }
    public void setReportingId(Long reportingId) { this.reportingId = reportingId; }

    public String getNomUtilisateur() { return nomUtilisateur; }
    public void setNomUtilisateur(String nomUtilisateur) { this.nomUtilisateur = nomUtilisateur; }
}
