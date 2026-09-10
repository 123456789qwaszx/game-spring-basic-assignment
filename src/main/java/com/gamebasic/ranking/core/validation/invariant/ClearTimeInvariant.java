package com.gamebasic.ranking.core.validation.invariant;

import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.RecordInvariant;
import com.gamebasic.ranking.core.validation.RecordVerdict;

public class ClearTimeInvariant implements RecordInvariant {

    private static final int MINIMUM_SECONDS_PER_FLOOR = 30;

    @Override
    public RecordVerdict evaluate(RankingRecord record) {
        long minimumDuration =
                record.clearedFloor() * (long) MINIMUM_SECONDS_PER_FLOOR;

        if(record.durationSeconds() < minimumDuration){
            return RecordVerdict.INVALID_DURATION;
        }

        return RecordVerdict.VALID;
    }
}