package com.gamebasic.ranking.core.validation.invariant;

import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.RecordInvariant;
import com.gamebasic.ranking.core.validation.RecordVerdict;

public final class DeckSizeInvariant implements RecordInvariant {

    private static final int MINIMUM_DECK_SIZE = 9;
    private static final int MAXIMUM_DECK_SIZE = 20;

    @Override
    public RecordVerdict evaluate(RankingRecord record) {
        RankingRecord.Deck deck = record.deck();

        int actualSize = deck.cards().size();

        boolean isInRange =
                actualSize >= MINIMUM_DECK_SIZE
                        && actualSize <= MAXIMUM_DECK_SIZE;

        boolean matchesDeclaredSize =
                deck.declaredSize() == actualSize;

        if (!isInRange || !matchesDeclaredSize) {
            return RecordVerdict.INVALID_DECK_SIZE;
        }

        return RecordVerdict.VALID;
    }
}