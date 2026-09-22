package com.ocp.pdr.dto.response;

import java.util.List;
import java.util.Map;

public class PredictionIaResultDto {
    private String codeSAP;
    private String description;
    private Double quantiteInstallee;
    private Double consommationActuelle;
    private Double consommationPredite;
    private Double seuil;
    private Double ratio;
    private Integer scoreRisque; // 0 à 100
    private String niveauRisque; // FAIBLE, MOYEN, ELEVE, CRITIQUE, NON_EVALUABLE
    private Boolean alerte;
    private Double ratioRisque; // pour rétro-compatibilité
    private String statut;
    private String message;
    private List<String> explications;
    private Map<String, Object> features;
    private List<Map<String, Object>> historique;
    private Integer nbMoisHistorique;
    private String moisActuel;
    private String moisPrevu;
    private String modeleUtilise;

    public PredictionIaResultDto() {}

    public PredictionIaResultDto(String codeSAP, String description, Double quantiteInstallee,
                                 Double consommationActuelle, Double consommationPredite,
                                 Double seuil, Double ratio, Integer scoreRisque, String niveauRisque,
                                 Boolean alerte, Double ratioRisque, String statut, String message,
                                 List<String> explications, Map<String, Object> features,
                                 List<Map<String, Object>> historique, Integer nbMoisHistorique,
                                 String moisActuel, String moisPrevu,
                                 String modeleUtilise) {
        this.codeSAP = codeSAP;
        this.description = description;
        this.quantiteInstallee = quantiteInstallee;
        this.consommationActuelle = consommationActuelle;
        this.consommationPredite = consommationPredite;
        this.seuil = seuil;
        this.ratio = ratio;
        this.scoreRisque = scoreRisque;
        this.niveauRisque = niveauRisque;
        this.alerte = alerte;
        this.ratioRisque = ratioRisque;
        this.statut = statut;
        this.message = message;
        this.explications = explications;
        this.features = features;
        this.historique = historique;
        this.nbMoisHistorique = nbMoisHistorique;
        this.moisActuel = moisActuel;
        this.moisPrevu = moisPrevu;
        this.modeleUtilise = modeleUtilise;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String codeSAP;
        private String description;
        private Double quantiteInstallee;
        private Double consommationActuelle;
        private Double consommationPredite;
        private Double seuil;
        private Double ratio;
        private Integer scoreRisque;
        private String niveauRisque;
        private Boolean alerte;
        private Double ratioRisque;
        private String statut;
        private String message;
        private List<String> explications;
        private Map<String, Object> features;
        private List<Map<String, Object>> historique;
        private Integer nbMoisHistorique;
        private String moisActuel;
        private String moisPrevu;
        private String modeleUtilise;

        public Builder codeSAP(String codeSAP) { this.codeSAP = codeSAP; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder quantiteInstallee(Double quantiteInstallee) { this.quantiteInstallee = quantiteInstallee; return this; }
        public Builder consommationActuelle(Double consommationActuelle) { this.consommationActuelle = consommationActuelle; return this; }
        public Builder consommationPredite(Double consommationPredite) { this.consommationPredite = consommationPredite; return this; }
        public Builder seuil(Double seuil) { this.seuil = seuil; return this; }
        public Builder ratio(Double ratio) { this.ratio = ratio; return this; }
        public Builder scoreRisque(Integer scoreRisque) { this.scoreRisque = scoreRisque; return this; }
        public Builder niveauRisque(String niveauRisque) { this.niveauRisque = niveauRisque; return this; }
        public Builder alerte(Boolean alerte) { this.alerte = alerte; return this; }
        public Builder ratioRisque(Double ratioRisque) { this.ratioRisque = ratioRisque; return this; }
        public Builder statut(String statut) { this.statut = statut; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder explications(List<String> explications) { this.explications = explications; return this; }
        public Builder features(Map<String, Object> features) { this.features = features; return this; }
        public Builder historique(List<Map<String, Object>> historique) { this.historique = historique; return this; }
        public Builder nbMoisHistorique(Integer nbMoisHistorique) { this.nbMoisHistorique = nbMoisHistorique; return this; }
        public Builder moisActuel(String moisActuel) { this.moisActuel = moisActuel; return this; }
        public Builder moisPrevu(String moisPrevu) { this.moisPrevu = moisPrevu; return this; }
        public Builder modeleUtilise(String modeleUtilise) { this.modeleUtilise = modeleUtilise; return this; }

        public PredictionIaResultDto build() {
            return new PredictionIaResultDto(codeSAP, description, quantiteInstallee, consommationActuelle,
                    consommationPredite, seuil, ratio, scoreRisque, niveauRisque, alerte, ratioRisque,
                    statut, message, explications, features, historique, nbMoisHistorique, moisActuel, moisPrevu, modeleUtilise);
        }
    }

    public String getCodeSAP() { return codeSAP; }
    public void setCodeSAP(String codeSAP) { this.codeSAP = codeSAP; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getQuantiteInstallee() { return quantiteInstallee; }
    public void setQuantiteInstallee(Double quantiteInstallee) { this.quantiteInstallee = quantiteInstallee; }

    public Double getConsommationActuelle() { return consommationActuelle; }
    public void setConsommationActuelle(Double consommationActuelle) { this.consommationActuelle = consommationActuelle; }

    public Double getConsommationPredite() { return consommationPredite; }
    public void setConsommationPredite(Double consommationPredite) { this.consommationPredite = consommationPredite; }

    public Double getSeuil() { return seuil; }
    public void setSeuil(Double seuil) { this.seuil = seuil; }

    public Double getRatio() { return ratio; }
    public void setRatio(Double ratio) { this.ratio = ratio; }

    public Integer getScoreRisque() { return scoreRisque; }
    public void setScoreRisque(Integer scoreRisque) { this.scoreRisque = scoreRisque; }

    public String getNiveauRisque() { return niveauRisque; }
    public void setNiveauRisque(String niveauRisque) { this.niveauRisque = niveauRisque; }

    public Boolean getAlerte() { return alerte; }
    public void setAlerte(Boolean alerte) { this.alerte = alerte; }

    public Double getRatioRisque() { return ratioRisque; }
    public void setRatioRisque(Double ratioRisque) { this.ratioRisque = ratioRisque; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getExplications() { return explications; }
    public void setExplications(List<String> explications) { this.explications = explications; }

    public Map<String, Object> getFeatures() { return features; }
    public void setFeatures(Map<String, Object> features) { this.features = features; }

    public List<Map<String, Object>> getHistorique() { return historique; }
    public void setHistorique(List<Map<String, Object>> historique) { this.historique = historique; }

    public Integer getNbMoisHistorique() { return nbMoisHistorique; }
    public void setNbMoisHistorique(Integer nbMoisHistorique) { this.nbMoisHistorique = nbMoisHistorique; }

    public String getMoisActuel() { return moisActuel; }
    public void setMoisActuel(String moisActuel) { this.moisActuel = moisActuel; }

    public String getMoisPrevu() { return moisPrevu; }
    public void setMoisPrevu(String moisPrevu) { this.moisPrevu = moisPrevu; }

    public String getModeleUtilise() { return modeleUtilise; }
    public void setModeleUtilise(String modeleUtilise) { this.modeleUtilise = modeleUtilise; }
}
