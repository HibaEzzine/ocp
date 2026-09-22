package com.ocp.pdr.dto.response;

public class ApiErrorResponse {

    private boolean success;
    private String message;
    private long timestamp;

    public ApiErrorResponse() {}

    public ApiErrorResponse(boolean success, String message, long timestamp) {
        this.success = success;
        this.message = message;
        this.timestamp = timestamp;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean success;
        private String message;
        private long timestamp;

        public Builder success(boolean success) { this.success = success; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder timestamp(long timestamp) { this.timestamp = timestamp; return this; }

        public ApiErrorResponse build() {
            return new ApiErrorResponse(success, message, timestamp);
        }
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
