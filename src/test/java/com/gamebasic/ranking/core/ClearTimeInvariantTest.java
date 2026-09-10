package com.gamebasic.ranking.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gamebasic.ranking.core.RankingRecordFixture.validCandidate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClearTimeInvariantTest {

    private ClearTimeInvariant invariant;

    @BeforeEach
    void setUp() {
        invariant = new ClearTimeInvariant();
    }

    @Test
    @DisplayName("10층 클리어 시간이 300초이면 통과한다")
    void minimumDurationIsValid() {
        RankingRecord record =
                validCandidate()
                        .durationSeconds(300)
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.VALID,
                verdict
        );
    }

    @Test
    @DisplayName("10층 클리어 시간이 299초이면 위반한다")
    void belowMinimumDurationIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .durationSeconds(299)
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_DURATION,
                verdict
        );
    }

    @Test
    @DisplayName("최소 시간보다 오래 걸린 기록은 통과한다")
    void aboveMinimumDurationIsValid() {
        RankingRecord record =
                validCandidate()
                        .durationSeconds(301)
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.VALID,
                verdict
        );
    }
}