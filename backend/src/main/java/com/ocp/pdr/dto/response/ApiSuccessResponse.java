package com.ocp.pdr.dto.response;

public class ApiSuccessResponse {

    private boolean success;
    private String message;
    private long resultCount;
    private long timestamp;

    public ApiSuccessResponse() {}

    public ApiSuccessResponse(boolean success, String message, long resultCount, long timestamp) {
        this.success = success;
        this.message = message;
        this.resultCount = resultCount;
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean success;
        private String message;
        private long resultCount;
        private long timestamp;

        public Builder success(boolean success) { this.success = success; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder resultCount(long resultCount) { this.resultCount = resultCount; return this; }
        public Builder timestamp(long timestamp) { this.timestamp = timestamp; return this; }

        public ApiSuccessResponse build() {
            return new ApiSuccessResponse(success, message, resultCount, timestamp);
        }
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getResultCount() { return resultCount; }
    public void setResultCount(long resultCount) { this.resultCount = resultCount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
