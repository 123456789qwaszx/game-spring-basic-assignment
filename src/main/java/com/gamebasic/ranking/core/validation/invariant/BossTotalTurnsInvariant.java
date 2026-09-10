package com.gamebasic.ranking.core.validation.invariant;

import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.core.validation.RecordVerdict;

public final class BossTotalTurnsInvariant implements RecordInvariant {

    @Override
    public RecordVerdict evaluate(RankingRecord record) {
        long actualTotalTurns =
                record.bossFight()
                        .phases()
                        .stream()
                        .mapToLong(
                                RankingRecord.BossFight.Phase::turns
                        )
                        .sum();

        long declaredTotalTurns =
                record.bossFight().totalTurns();

        if (declaredTotalTurns != actualTotalTurns) {
            return RecordVerdict.INVALID_BOSS_TOTAL_TURNS;
        }

        return RecordVerdict.VALID;
    }
}