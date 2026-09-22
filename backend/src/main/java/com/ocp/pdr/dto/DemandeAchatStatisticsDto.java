package com.ocp.pdr.dto;

import java.util.Map;

public class DemandeAchatStatisticsDto {
    private long totalReportings;
    private Map<String, Long> reportingsByStatut;
    private Map<String, Long> reportingsBySecteur;
    private Map<String, Long> reportingsByResponsable;
    private double totalQuantite;
    private double averageQuantite;
    private double averagePourcentageLivraison;
    private double averageDelaiLivraison;

    public DemandeAchatStatisticsDto() {}

    public DemandeAchatStatisticsDto(long totalReportings, Map<String, Long> reportingsByStatut,
                                     Map<String, Long> reportingsBySecteur, Map<String, Long> reportingsByResponsable,
                                     double totalQuantite, double averageQuantite,
                                     double averagePourcentageLivraison, double averageDelaiLivraison) {
        this.totalReportings = totalReportings;
        this.reportingsByStatut = reportingsByStatut;
        this.reportingsBySecteur = reportingsBySecteur;
        this.reportingsByResponsable = reportingsByResponsable;
        this.totalQuantite = totalQuantite;
        this.averageQuantite = averageQuantite;
        this.averagePourcentageLivraison = averagePourcentageLivraison;
        this.averageDelaiLivraison = averageDelaiLivraison;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long totalReportings;
        private Map<String, Long> reportingsByStatut;
        private Map<String, Long> reportingsBySecteur;
        private Map<String, Long> reportingsByResponsable;
        private double totalQuantite;
        private double averageQuantite;
        private double averagePourcentageLivraison;
        private double averageDelaiLivraison;

        public Builder totalReportings(long totalReportings) { this.totalReportings = totalReportings; return this; }
        public Builder reportingsByStatut(Map<String, Long> reportingsByStatut) { this.reportingsByStatut = reportingsByStatut; return this; }
        public Builder reportingsBySecteur(Map<String, Long> reportingsBySecteur) { this.reportingsBySecteur = reportingsBySecteur; return this; }
        public Builder reportingsByResponsable(Map<String, Long> reportingsByResponsable) { this.reportingsByResponsable = reportingsByResponsable; return this; }
        public Builder totalQuantite(double totalQuantite) { this.totalQuantite = totalQuantite; return this; }
        public Builder averageQuantite(double averageQuantite) { this.averageQuantite = averageQuantite; return this; }
        public Builder averagePourcentageLivraison(double averagePourcentageLivraison) { this.averagePourcentageLivraison = averagePourcentageLivraison; return this; }
        public Builder averageDelaiLivraison(double averageDelaiLivraison) { this.averageDelaiLivraison = averageDelaiLivraison; return this; }

        public DemandeAchatStatisticsDto build() {
            return new DemandeAchatStatisticsDto(totalReportings, reportingsByStatut, reportingsBySecteur,
                    reportingsByResponsable, totalQuantite, averageQuantite, averagePourcentageLivraison, averageDelaiLivraison);
        }
    }

    public long getTotalReportings() { return totalReportings; }
    public void setTotalReportings(long totalReportings) { this.totalReportings = totalReportings; }

    public Map<String, Long> getReportingsByStatut() { return reportingsByStatut; }
    public void setReportingsByStatut(Map<String, Long> reportingsByStatut) { this.reportingsByStatut = reportingsByStatut; }

    public Map<String, Long> getReportingsBySecteur() { return reportingsBySecteur; }
    public void setReportingsBySecteur(Map<String, Long> reportingsBySecteur) { this.reportingsBySecteur = reportingsBySecteur; }

    public Map<String, Long> getReportingsByResponsable() { return reportingsByResponsable; }
    public void setReportingsByResponsable(Map<String, Long> reportingsByResponsable) { this.reportingsByResponsable = reportingsByResponsable; }

    public double getTotalQuantite() { return totalQuantite; }
    public void setTotalQuantite(double totalQuantite) { this.totalQuantite = totalQuantite; }

    public double getAverageQuantite() { return averageQuantite; }
    public void setAverageQuantite(double averageQuantite) { this.averageQuantite = averageQuantite; }

    public double getAveragePourcentageLivraison() { return averagePourcentageLivraison; }
    public void setAveragePourcentageLivraison(double averagePourcentageLivraison) { this.averagePourcentageLivraison = averagePourcentageLivraison; }

    public double getAverageDelaiLivraison() { return averageDelaiLivraison; }
    public void setAverageDelaiLivraison(double averageDelaiLivraison) { this.averageDelaiLivraison = averageDelaiLivraison; }
}
