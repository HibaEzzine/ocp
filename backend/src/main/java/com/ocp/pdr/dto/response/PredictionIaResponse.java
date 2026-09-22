package com.ocp.pdr.dto.response;

import java.util.List;
import java.util.Map;

public class PredictionIaResponse {
    private Boolean success;
    private Integer totalArticles;
    private Integer nbCritique;
    private Integer nbAlertes;
    private Integer nbRisqueMoyen;
    private Integer nbNormal;
    private Integer nbInsuffisant;
    private List<PredictionIaResultDto> predictions;
    private Map<String, Object> evaluation;
    private Map<String, Object> dataset;
    private String modelStatus;
    private String message;
    private Long timestamp;

    public PredictionIaResponse() {}

    public PredictionIaResponse(Boolean success, Integer totalArticles, Integer nbCritique, Integer nbAlertes,
                                Integer nbRisqueMoyen, Integer nbNormal, Integer nbInsuffisant,
                                List<PredictionIaResultDto> predictions, Map<String, Object> evaluation,
                                Map<String, Object> dataset, String modelStatus, String message, Long timestamp) {
        this.success = success;
        this.totalArticles = totalArticles;
        this.nbCritique = nbCritique;
        this.nbAlertes = nbAlertes;
        this.nbRisqueMoyen = nbRisqueMoyen;
        this.nbNormal = nbNormal;
        this.nbInsuffisant = nbInsuffisant;
        this.predictions = predictions;
        this.evaluation = evaluation;
        this.dataset = dataset;
        this.modelStatus = modelStatus;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Boolean success;
        private Integer totalArticles;
        private Integer nbCritique;
        private Integer nbAlertes;
        private Integer nbRisqueMoyen;
        private Integer nbNormal;
        private Integer nbInsuffisant;
        private List<PredictionIaResultDto> predictions;
        private Map<String, Object> evaluation;
        private Map<String, Object> dataset;
        private String modelStatus;
        private String message;
        private Long timestamp;

        public Builder success(Boolean success) { this.success = success; return this; }
        public Builder totalArticles(Integer totalArticles) { this.totalArticles = totalArticles; return this; }
        public Builder nbCritique(Integer nbCritique) { this.nbCritique = nbCritique; return this; }
        public Builder nbAlertes(Integer nbAlertes) { this.nbAlertes = nbAlertes; return this; }
        public Builder nbRisqueMoyen(Integer nbRisqueMoyen) { this.nbRisqueMoyen = nbRisqueMoyen; return this; }
        public Builder nbNormal(Integer nbNormal) { this.nbNormal = nbNormal; return this; }
        public Builder nbInsuffisant(Integer nbInsuffisant) { this.nbInsuffisant = nbInsuffisant; return this; }
        public Builder predictions(List<PredictionIaResultDto> predictions) { this.predictions = predictions; return this; }
        public Builder evaluation(Map<String, Object> evaluation) { this.evaluation = evaluation; return this; }
        public Builder dataset(Map<String, Object> dataset) { this.dataset = dataset; return this; }
        public Builder modelStatus(String modelStatus) { this.modelStatus = modelStatus; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder timestamp(Long timestamp) { this.timestamp = timestamp; return this; }

        public PredictionIaResponse build() {
            return new PredictionIaResponse(success, totalArticles, nbCritique, nbAlertes, nbRisqueMoyen, nbNormal,
                    nbInsuffisant, predictions, evaluation, dataset, modelStatus, message, timestamp);
        }
    }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }

    public Integer getTotalArticles() { return totalArticles; }
    public void setTotalArticles(Integer totalArticles) { this.totalArticles = totalArticles; }

    public Integer nbCritique() { return nbCritique; }
    public Integer getNbCritique() { return nbCritique; }
    public void setNbCritique(Integer nbCritique) { this.nbCritique = nbCritique; }

    public Integer getNbAlertes() { return nbAlertes; }
    public void setNbAlertes(Integer nbAlertes) { this.nbAlertes = nbAlertes; }

    public Integer getNbRisqueMoyen() { return nbRisqueMoyen; }
    public void setNbRisqueMoyen(Integer nbRisqueMoyen) { this.nbRisqueMoyen = nbRisqueMoyen; }

    public Integer getNbNormal() { return nbNormal; }
    public void setNbNormal(Integer nbNormal) { this.nbNormal = nbNormal; }

    public Integer getNbInsuffisant() { return nbInsuffisant; }
    public void setNbInsuffisant(Integer nbInsuffisant) { this.nbInsuffisant = nbInsuffisant; }

    public List<PredictionIaResultDto> getPredictions() { return predictions; }
    public void setPredictions(List<PredictionIaResultDto> predictions) { this.predictions = predictions; }

    public Map<String, Object> getEvaluation() { return evaluation; }
    public void setEvaluation(Map<String, Object> evaluation) { this.evaluation = evaluation; }

    public Map<String, Object> getDataset() { return dataset; }
    public void setDataset(Map<String, Object> dataset) { this.dataset = dataset; }

    public String getModelStatus() { return modelStatus; }
    public void setModelStatus(String modelStatus) { this.modelStatus = modelStatus; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
