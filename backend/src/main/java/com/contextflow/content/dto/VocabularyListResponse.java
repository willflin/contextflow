package com.contextflow.content.dto;

import java.util.List;

public record VocabularyListResponse(
        String statusFilter,
        String query,
        int limit,
        int totalMatchedWords,
        List<VocabularyWordResponse> items
) {
}
