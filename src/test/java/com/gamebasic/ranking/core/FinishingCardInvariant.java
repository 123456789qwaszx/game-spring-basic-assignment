package com.gamebasic.ranking.core;

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