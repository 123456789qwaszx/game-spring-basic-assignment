package com.gamebasic.ranking.core.validation.invariant;

import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.RecordInvariant;
import com.gamebasic.ranking.core.validation.RecordVerdict;

public final class BossPhaseTurnsInvariant implements RecordInvariant {

    private static final int MINIMUM_PHASE_TURNS = 1;

    @Override
    public RecordVerdict evaluate(RankingRecord record) {
        for (RankingRecord.BossFight.Phase phase
                : record.bossFight().phases()) {

            if (phase.turns() < MINIMUM_PHASE_TURNS) {
                return RecordVerdict.INVALID_BOSS_PHASE_TURNS;
            }
        }

        return RecordVerdict.VALID;
    }
}