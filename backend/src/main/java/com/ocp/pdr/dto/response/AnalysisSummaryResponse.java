package com.ocp.pdr.dto.response;

public class AnalysisSummaryResponse {

    private long totalArticles;
    private long minMaxCount;
    private long planifieCount;
    private long surDemandeCount;
    private long anomaliesCount;
    private long articlesEnStock;
    private double minMaxRate;
    private double planifieRate;
    private double surDemandeRate;
    private double stockRate;

    public AnalysisSummaryResponse() {}

    public AnalysisSummaryResponse(long totalArticles, long minMaxCount, long planifieCount, long surDemandeCount,
                                   long anomaliesCount, long articlesEnStock, double minMaxRate, double planifieRate,
                                   double surDemandeRate, double stockRate) {
        this.totalArticles = totalArticles;
        this.minMaxCount = minMaxCount;
        this.planifieCount = planifieCount;
        this.surDemandeCount = surDemandeCount;
        this.anomaliesCount = anomaliesCount;
        this.articlesEnStock = articlesEnStock;
        this.minMaxRate = minMaxRate;
        this.planifieRate = planifieRate;
        this.surDemandeRate = surDemandeRate;
        this.stockRate = stockRate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long totalArticles;
        private long minMaxCount;
        private long planifieCount;
        private long surDemandeCount;
        private long anomaliesCount;
        private long articlesEnStock;
        private double minMaxRate;
        private double planifieRate;
        private double surDemandeRate;
        private double stockRate;

        public Builder totalArticles(long totalArticles) { this.totalArticles = totalArticles; return this; }
        public Builder minMaxCount(long minMaxCount) { this.minMaxCount = minMaxCount; return this; }
        public Builder planifieCount(long planifieCount) { this.planifieCount = planifieCount; return this; }
        public Builder surDemandeCount(long surDemandeCount) { this.surDemandeCount = surDemandeCount; return this; }
        public Builder anomaliesCount(long anomaliesCount) { this.anomaliesCount = anomaliesCount; return this; }
        public Builder articlesEnStock(long articlesEnStock) { this.articlesEnStock = articlesEnStock; return this; }
        public Builder minMaxRate(double minMaxRate) { this.minMaxRate = minMaxRate; return this; }
        public Builder planifieRate(double planifieRate) { this.planifieRate = planifieRate; return this; }
        public Builder surDemandeRate(double surDemandeRate) { this.surDemandeRate = surDemandeRate; return this; }
        public Builder stockRate(double stockRate) { this.stockRate = stockRate; return this; }

        public AnalysisSummaryResponse build() {
            return new AnalysisSummaryResponse(totalArticles, minMaxCount, planifieCount, surDemandeCount, anomaliesCount,
                    articlesEnStock, minMaxRate, planifieRate, surDemandeRate, stockRate);
        }
    }

    public long getTotalArticles() { return totalArticles; }
    public void setTotalArticles(long totalArticles) { this.totalArticles = totalArticles; }

    public long getMinMaxCount() { return minMaxCount; }
    public void setMinMaxCount(long minMaxCount) { this.minMaxCount = minMaxCount; }

    public long getPlanifieCount() { return planifieCount; }
    public void setPlanifieCount(long planifieCount) { this.planifieCount = planifieCount; }

    public long getSurDemandeCount() { return surDemandeCount; }
    public void setSurDemandeCount(long surDemandeCount) { this.surDemandeCount = surDemandeCount; }

    public long getAnomaliesCount() { return anomaliesCount; }
    public void setAnomaliesCount(long anomaliesCount) { this.anomaliesCount = anomaliesCount; }

    public long getArticlesEnStock() { return articlesEnStock; }
    public void setArticlesEnStock(long articlesEnStock) { this.articlesEnStock = articlesEnStock; }

    public double getMinMaxRate() { return minMaxRate; }
    public void setMinMaxRate(double minMaxRate) { this.minMaxRate = minMaxRate; }

    public double getPlanifieRate() { return planifieRate; }
    public void setPlanifieRate(double planifieRate) { this.planifieRate = planifieRate; }

    public double getSurDemandeRate() { return surDemandeRate; }
    public void setSurDemandeRate(double surDemandeRate) { this.surDemandeRate = surDemandeRate; }

    public double getStockRate() { return stockRate; }
    public void setStockRate(double stockRate) { this.stockRate = stockRate; }
}
