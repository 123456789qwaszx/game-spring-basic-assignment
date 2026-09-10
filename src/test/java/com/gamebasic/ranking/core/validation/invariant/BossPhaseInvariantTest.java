package com.gamebasic.ranking.core.validation.invariant;

import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.core.validation.RecordVerdict;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gamebasic.ranking.testfixture.RankingRecordFixture.bossFightWithPhases;
import static com.gamebasic.ranking.testfixture.RankingRecordFixture.phase;
import static com.gamebasic.ranking.testfixture.RankingRecordFixture.validCandidate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BossPhaseInvariantTest {

    private BossPhaseInvariant invariant;

    @BeforeEach
    void setUp() {
        invariant = new BossPhaseInvariant();
    }

    @Test
    @DisplayName("보스 페이즈가 THRONE, UNBOUND, ECLIPSE 순서이면 통과한다")
    void requiredPhaseSequenceIsValid() {
        RankingRecord record =
                validCandidate().build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.VALID,
                verdict
        );
    }

    @Test
    @DisplayName("보스 페이즈가 3개보다 적으면 위반한다")
    void fewerThanThreePhasesIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .bossFight(
                                bossFightWithPhases(
                                        List.of(
                                                phase("THRONE", 1, 0),
                                                phase("UNBOUND", 1, 0)
                                        )
                                )
                        )
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_BOSS_PHASES,
                verdict
        );
    }

    @Test
    @DisplayName("보스 페이즈가 3개보다 많으면 위반한다")
    void moreThanThreePhasesIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .bossFight(
                                bossFightWithPhases(
                                        List.of(
                                                phase("THRONE", 1, 0),
                                                phase("UNBOUND", 1, 0),
                                                phase("ECLIPSE", 1, 0),
                                                phase("EXTRA", 1, 0)
                                        )
                                )
                        )
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_BOSS_PHASES,
                verdict
        );
    }

    @Test
    @DisplayName("보스 페이즈의 순서가 다르면 위반한다")
    void wrongPhaseOrderIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .bossFight(
                                bossFightWithPhases(
                                        List.of(
                                                phase("UNBOUND", 1, 0),
                                                phase("THRONE", 1, 0),
                                                phase("ECLIPSE", 1, 0)
                                        )
                                )
                        )
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_BOSS_PHASES,
                verdict
        );
    }
}