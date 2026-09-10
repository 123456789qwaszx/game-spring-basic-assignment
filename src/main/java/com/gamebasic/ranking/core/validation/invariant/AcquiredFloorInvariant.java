package com.gamebasic.ranking.core.validation.invariant;

import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.RecordInvariant;
import com.gamebasic.ranking.core.validation.RecordVerdict;

public final class AcquiredFloorInvariant implements RecordInvariant {

    private static final int MINIMUM_ACQUIRED_FLOOR = 0;
    private static final int MAXIMUM_ACQUIRED_FLOOR = 9;

    public RecordVerdict evaluate(RankingRecord record) {
        for (RankingRecord.Deck.Card card : record.deck().cards()) {
            int acquiredFloor = card.acquiredFloor();

            if (acquiredFloor < MINIMUM_ACQUIRED_FLOOR
                    || acquiredFloor > MAXIMUM_ACQUIRED_FLOOR) {

                return RecordVerdict.INVALID_ACQUIRED_FLOOR;
            }
        }

        return RecordVerdict.VALID;
    }
}