package com.gamebasic.ranking.core.validation.invariant;

import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.core.validation.RecordVerdict;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gamebasic.ranking.testfixture.RankingRecordFixture.validCandidate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FinalHpInvariantTest {

    private FinalHpInvariant invariant;

    @BeforeEach
    void setUp() {
        invariant = new FinalHpInvariant();
    }

    @Test
    @DisplayName("남은 HP가 1이면 통과한다")
    void minimumHpIsValid() {
        RankingRecord record =
                validCandidate()
                        .finalHp(1)
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.VALID,
                verdict
        );
    }

    @Test
    @DisplayName("남은 HP가 99이면 통과한다")
    void maximumHpIsValid() {
        RankingRecord record =
                validCandidate()
                        .finalHp(99)
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.VALID,
                verdict
        );
    }

    @Test
    @DisplayName("남은 HP가 0이면 위반한다")
    void zeroHpIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .finalHp(0)
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_HP,
                verdict
        );
    }

    @Test
    @DisplayName("남은 HP가 100이면 위반한다")
    void overMaximumHpIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .finalHp(100)
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_HP,
                verdict
        );
    }
}