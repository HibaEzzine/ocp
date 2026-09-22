package com.ocp.pdr.dto.response;

import java.util.ArrayList;
import java.util.List;

public record ImportResult(
        boolean success,
        String message,
        int lignesImportees,
        int erreurs,
        int totalRows,
        int rejectedRows,
        int duplicateRows,
        List<String> detailsErreurs) {

    public ImportResult {
        detailsErreurs = detailsErreurs == null ? List.of() : List.copyOf(detailsErreurs);
    }

    public static ImportResult from(ImportResultBuilder builder) {
        boolean success = builder.errors.isEmpty() && builder.importedLines > 0;
        String message = success
                ? "Importation réussie : " + String.join(", ", builder.details)
                : builder.importedLines > 0
                    ? "Importation partielle : " + String.join(", ", builder.details)
                    : "Importation échouée : aucune ligne valide importée.";
        return new ImportResult(
                success,
                message,
                builder.importedLines,
                builder.errors.size(),
                builder.totalRows,
                builder.rejectedRows,
                builder.duplicateRows,
                builder.errors
        );
    }

    public static final class ImportResultBuilder {
        private final List<String> details = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();
        private int importedLines;
        private int totalRows;
        private int rejectedRows;
        private int duplicateRows;

        public void addDetail(int count, String label) {
            if (count > 0) {
                details.add(count + " " + label);
                importedLines += count;
            }
        }

        public void addError(String error) {
            errors.add(error);
        }

        public void incrementTotalRows() {
            totalRows++;
        }

        public void incrementRejectedRows() {
            rejectedRows++;
        }

        public void incrementDuplicateRows() {
            duplicateRows++;
        }

        public List<String> getErrors() {
            return errors;
        }
    }
}
