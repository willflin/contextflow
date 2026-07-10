package com.contextflow.placement.domain;

public enum PlacementItemStatus {
    PENDING,
    GENERATING_TEXT,
    VALIDATING_TEXT,
    GENERATING_AUDIO,
    READY,
    FAILED,
    EXPIRED
}
