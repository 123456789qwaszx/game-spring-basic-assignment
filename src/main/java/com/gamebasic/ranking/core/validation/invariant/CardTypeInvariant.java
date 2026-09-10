package com.gamebasic.ranking.core.validation.invariant;

import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.core.validation.RecordVerdict;

import java.util.Set;

public final class CardTypeInvariant implements RecordInvariant {

    private final Set<String> allowedCardTypes;

    public CardTypeInvariant(Set<String> allowedCardTypes) {
        this.allowedCardTypes = Set.copyOf(allowedCardTypes);
    }

    @Override
    public RecordVerdict evaluate(RankingRecord record) {
        for (RankingRecord.Deck.Card card : record.deck().cards()) {
            if (!allowedCardTypes.contains(card.cardType())) {
                return RecordVerdict.INVALID_CARD_TYPE;
            }
        }

        return RecordVerdict.VALID;
    }
}