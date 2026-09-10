package com.gamebasic.ranking.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gamebasic.ranking.core.RankingRecordFixture.deck;
import static com.gamebasic.ranking.core.RankingRecordFixture.validCandidate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DeckSizeInvariantTest {

    private DeckSizeInvariant invariant;

    @BeforeEach
    void setUp() {
        invariant = new DeckSizeInvariant();
    }

    @Test
    @DisplayName("실제 카드가 9장이면 통과한다")
    void minimumDeckSizeIsValid() {
        RankingRecord record =
                validCandidate()
                        .deck(deck(9, 9))
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.VALID,
                verdict
        );
    }

    @Test
    @DisplayName("실제 카드가 20장이면 통과한다")
    void maximumDeckSizeIsValid() {
        RankingRecord record =
                validCandidate()
                        .deck(deck(20, 20))
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.VALID,
                verdict
        );
    }

    @Test
    @DisplayName("실제 카드가 8장이면 위반한다")
    void belowMinimumDeckSizeIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .deck(deck(8, 8))
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_DECK_SIZE,
                verdict
        );
    }

    @Test
    @DisplayName("실제 카드가 21장이면 위반한다")
    void aboveMaximumDeckSizeIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .deck(deck(21, 21))
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_DECK_SIZE,
                verdict
        );
    }

    @Test
    @DisplayName("선언한 크기와 실제 카드 수가 다르면 위반한다")
    void declaredSizeMismatchIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .deck(deck(10, 9))
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_DECK_SIZE,
                verdict
        );
    }
}