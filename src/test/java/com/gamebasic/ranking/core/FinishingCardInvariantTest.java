package com.gamebasic.ranking.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gamebasic.ranking.core.RankingRecordFixture.bossFightWithFinishingCard;
import static com.gamebasic.ranking.core.RankingRecordFixture.validCandidate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FinishingCardInvariantTest {

    private FinishingCardInvariant invariant;

    @BeforeEach
    void setUp() {
        invariant = new FinishingCardInvariant();
    }

    @Test
    @DisplayName("마무리 카드가 최종 덱에 있으면 통과한다")
    void finishingCardInDeckIsValid() {
        RankingRecord record =
                validCandidate()
                        .bossFight(
                                bossFightWithFinishingCard(
                                        "HEART_PIERCE"
                                )
                        )
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.VALID,
                verdict
        );
    }

    @Test
    @DisplayName("마무리 카드가 최종 덱에 없으면 위반한다")
    void finishingCardNotInDeckIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .bossFight(
                                bossFightWithFinishingCard(
                                        "SECOND_HEART"
                                )
                        )
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_FINISHING_CARD,
                verdict
        );
    }
}