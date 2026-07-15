package com.contextflow.content.dto;

import java.util.List;

public record LowLevelMasteryRequest(
        List<Long> senseIds
) {
}
