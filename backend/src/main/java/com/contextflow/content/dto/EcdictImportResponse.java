package com.contextflow.content.dto;

public record EcdictImportResponse(
        Long batchId,
        String fileName,
        String fileSha256,
        int loadedRows,
        int cleanRows,
        int learningUnitRows,
        int learningSenseRows,
        boolean reusedExistingBatch
) {
}
