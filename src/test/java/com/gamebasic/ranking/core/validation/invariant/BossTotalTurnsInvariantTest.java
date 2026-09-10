package com.gamebasic.ranking.core.validation.invariant;

import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.core.validation.RecordVerdict;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gamebasic.ranking.testfixture.RankingRecordFixture.bossFightWithTurns;
import static com.gamebasic.ranking.testfixture.RankingRecordFixture.bossFightWithTurnsAndTotal;
import static com.gamebasic.ranking.testfixture.RankingRecordFixture.validCandidate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BossTotalTurnsInvariantTest {

    private BossTotalTurnsInvariant invariant;

    @BeforeEach
    void setUp() {
        invariant = new BossTotalTurnsInvariant();
    }

    @Test
    @DisplayName("보스 총 턴 수가 각 페이즈 턴 수의 합과 같으면 통과한다")
    void matchingTotalTurnsIsValid() {
        RankingRecord record =
                validCandidate()
                        .bossFight(
                                bossFightWithTurns(
                                        2,
                                        3,
                                        4
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
    @DisplayName("보스 총 턴 수가 페이즈 턴 수의 합보다 작으면 위반한다")
    void totalTurnsBelowPhaseSumIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .bossFight(
                                bossFightWithTurnsAndTotal(
                                        2,
                                        3,
                                        4,
                                        8
                                )
                        )
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_BOSS_TOTAL_TURNS,
                verdict
        );
    }

    @Test
    @DisplayName("보스 총 턴 수가 페이즈 턴 수의 합보다 크면 위반한다")
    void totalTurnsAbovePhaseSumIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .bossFight(
                                bossFightWithTurnsAndTotal(
                                        2,
                                        3,
                                        4,
                                        10
                                )
                        )
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_BOSS_TOTAL_TURNS,
                verdict
        );
    }
}