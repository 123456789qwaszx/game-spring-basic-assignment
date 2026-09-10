package com.gamebasic.ranking.core;

import java.util.List;
import java.util.stream.IntStream;

final class RankingRecordFixture {

    static final String VALID_STATUS = "CLEARED";
    static final int VALID_CLEARED_FLOOR = 10;
    static final int VALID_DURATION_SECONDS = 300;
    static final int VALID_FINAL_HP = 99;
    static final int VALID_DECK_SIZE = 9;

    static final RankingRecord.Deck VALID_DECK =
            new RankingRecord.Deck(
                    VALID_DECK_SIZE,
                    List.of(
                            card("STRIKE", 0),
                            card("STRIKE", 0),
                            card("HEART_PIERCE", 0),
                            card("GUARD", 0),
                            card("MIST_KNOT", 0),
                            card("QUICK_SLASH", 0),
                            card("WARDING_SLASH", 0),
                            card("BLOOD_RUNE", 0),
                            card("MEND", 0)
                    )
            );

    private RankingRecordFixture() {
    }

    static Builder validCandidate() {
        return new Builder();
    }

    static RankingRecord.Deck deck(
            Integer declaredSize,
            int actualSize
    ) {
        List<RankingRecord.Deck.Card> cards =
                IntStream.range(0, actualSize)
                        .mapToObj(index ->
                                card("STRIKE", 0)
                        )
                        .toList();

        return new RankingRecord.Deck(
                declaredSize,
                cards
        );
    }

    static RankingRecord.Deck.Card card(
            String cardType,
            Integer acquiredFloor
    ) {
        return new RankingRecord.Deck.Card(
                cardType,
                acquiredFloor
        );
    }

    static final class Builder {

        private String status = VALID_STATUS;
        private Integer clearedFloor = VALID_CLEARED_FLOOR;
        private Integer durationSeconds = VALID_DURATION_SECONDS;
        private Integer finalHp = VALID_FINAL_HP;
        private RankingRecord.Deck deck = VALID_DECK;

        Builder status(String status) {
            this.status = status;
            return this;
        }

        Builder clearedFloor(Integer clearedFloor) {
            this.clearedFloor = clearedFloor;
            return this;
        }

        Builder durationSeconds(Integer durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        Builder finalHp(Integer finalHp) {
            this.finalHp = finalHp;
            return this;
        }

        Builder deck(RankingRecord.Deck deck) {
            this.deck = deck;
            return this;
        }

        RankingRecord build() {
            return new RankingRecord(
                    status,
                    clearedFloor,
                    durationSeconds,
                    finalHp,
                    deck
            );
        }
    }
}