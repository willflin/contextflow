package com.contextflow.learning.domain;

public enum LearningPackageStatus {
    PENDING,
    GENERATING_TEXT,
    VALIDATING_TEXT,
    GENERATING_AUDIO,
    READY,
    FAILED,
    EXPIRED,
    COMPLETED
}
