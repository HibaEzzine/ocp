package com.ocp.pdr.dto;

import java.util.ArrayList;
import java.util.List;

public class ImportResultDto {
    private int importedCount;
    private List<String> errors = new ArrayList<>();

    public ImportResultDto() {}

    public ImportResultDto(int importedCount, List<String> errors) {
        this.importedCount = importedCount;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int importedCount;
        private List<String> errors = new ArrayList<>();

        public Builder importedCount(int importedCount) { this.importedCount = importedCount; return this; }
        public Builder errors(List<String> errors) { this.errors = errors; return this; }

        public ImportResultDto build() {
            return new ImportResultDto(importedCount, errors);
        }
    }

    public int getImportedCount() { return importedCount; }
    public void setImportedCount(int importedCount) { this.importedCount = importedCount; }

    public List<String> getErrors() { return errors; }
    public void setErrors(List<String> errors) { this.errors = errors; }
}
