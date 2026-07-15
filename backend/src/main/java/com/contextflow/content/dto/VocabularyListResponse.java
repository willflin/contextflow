package com.contextflow.content.dto;

import java.util.List;

public record VocabularyListResponse(
        String statusFilter,
        String query,
        int page,
        int pageSize,
        int totalMatchedWords,
        int totalPages,
        boolean hasNextPage,
        List<VocabularyWordResponse> items
) {
}
