package com.ocp.pdr.dto.response;

import java.util.List;

public record ExcelValidationResult(
        boolean valid,
        String message,
        String fileName,
        long fileSize,
        int totalRows,
        int validRows,
        int rejectedRows,
        List<String> missingColumns,
        List<String> errors
) {
    public ExcelValidationResult {
        missingColumns = missingColumns == null ? List.of() : List.copyOf(missingColumns);
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}
