package com.gamebasic.ranking.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gamebasic.ranking.core.RankingRecordFixture.bossFightWithTurns;
import static com.gamebasic.ranking.core.RankingRecordFixture.validCandidate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BossPhaseTurnsInvariantTest {

    private BossPhaseTurnsInvariant invariant;

    @BeforeEach
    void setUp() {
        invariant = new BossPhaseTurnsInvariant();
    }

    @Test
    @DisplayName("모든 보스 페이즈의 턴 수가 1이면 통과한다")
    void minimumTurnsForEveryPhaseIsValid() {
        RankingRecord record =
                validCandidate()
                        .bossFight(
                                bossFightWithTurns(
                                        1,
                                        1,
                                        1
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
    @DisplayName("모든 보스 페이즈의 턴 수가 1보다 크면 통과한다")
    void turnsAboveMinimumAreValid() {
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
    @DisplayName("보스 페이즈 중 하나의 턴 수가 0이면 위반한다")
    void zeroTurnPhaseIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .bossFight(
                                bossFightWithTurns(
                                        1,
                                        0,
                                        1
                                )
                        )
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_BOSS_PHASE_TURNS,
                verdict
        );
    }

    @Test
    @DisplayName("보스 페이즈 중 하나의 턴 수가 음수이면 위반한다")
    void negativeTurnPhaseIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .bossFight(
                                bossFightWithTurns(
                                        1,
                                        1,
                                        -1
                                )
                        )
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_BOSS_PHASE_TURNS,
                verdict
        );
    }
}