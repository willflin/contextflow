package com.contextflow.content.dto;

import java.util.List;

public record VocabularyWordResponse(
        Long learningUnitId,
        String canonicalText,
        String normalizedText,
        String learnedStatus,
        int learnedSenseCount,
        int totalSenseCount,
        List<VocabularySenseProgressResponse> senses
) {
}
