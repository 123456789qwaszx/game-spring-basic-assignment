package com.gamebasic.ranking.core.policy;

import com.gamebasic.ranking.core.model.RankingRecord;

import java.util.List;

public record RankingScreeningResult(
        List<RankingRecord> validRecords,
        int excludedCount
) {

    public RankingScreeningResult {
        validRecords = List.copyOf(validRecords);
    }
}
