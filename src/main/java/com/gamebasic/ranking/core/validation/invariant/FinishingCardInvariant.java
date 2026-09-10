package com.gamebasic.ranking.core.validation.invariant;

import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.RecordInvariant;
import com.gamebasic.ranking.core.validation.RecordVerdict;

public final class FinishingCardInvariant implements RecordInvariant {

    @Override
    public RecordVerdict evaluate(RankingRecord record) {
        String finishingCard =
                record.bossFight().finishingCard();

        for (RankingRecord.Deck.Card card : record.deck().cards()) {
            if (finishingCard.equals(card.cardType())) {
                return RecordVerdict.VALID;
            }
        }

        return RecordVerdict.INVALID_FINISHING_CARD;
    }
}