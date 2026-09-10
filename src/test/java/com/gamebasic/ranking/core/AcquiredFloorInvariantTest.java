package com.gamebasic.ranking.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gamebasic.ranking.core.RankingRecordFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AcquiredFloorInvariantTest {

    private AcquiredFloorInvariant invariant;

    @BeforeEach
    void setUp() {
        invariant = new AcquiredFloorInvariant();
    }

    @Test
    @DisplayName("카드 획득 층이 0이면 획득 층 조건을 통과한다")
    void minimumAcquiredFloorIsValid() {
        RankingRecord record =
                validCandidate()
                        .deck(
                                deckContaining(
                                        card("STRIKE", 0)
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
    @DisplayName("카드 획득 층이 9이면 획득 층 조건을 통과한다")
    void maximumAcquiredFloorIsValid() {
        RankingRecord record =
                validCandidate()
                        .deck(
                                deckContaining(
                                        card("STRIKE", 9)
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
    @DisplayName("카드 획득 층이 -1이면 획득 층 조건을 위반한다")
    void acquiredFloorBelowMinimumIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .deck(
                                deckContaining(
                                        card("STRIKE", -1)
                                )
                        )
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_ACQUIRED_FLOOR,
                verdict
        );
    }

    @Test
    @DisplayName("카드 획득 층이 10이면 획득 층 조건을 위반한다")
    void acquiredFloorAboveMaximumIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .deck(
                                deckContaining(
                                        card("STRIKE", 10)
                                )
                        )
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_ACQUIRED_FLOOR,
                verdict
        );
    }
}