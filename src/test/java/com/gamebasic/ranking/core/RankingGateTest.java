package com.gamebasic.ranking.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gamebasic.ranking.core.RankingRecordFixture.validCandidate;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("RankingGate")
class RankingGateTest {

    private RankingGate gate;

    @BeforeEach
    void setUp() {
        gate = new RankingGate();
    }

    @Test
    @DisplayName("CLEARED 상태로 10층을 클리어한 기록은 참가 대상이다")
    void clearedFinalFloorIsEligible() {
        RankingRecord record =
                validCandidate().build();

        GateDecision actual =
                gate.evaluate(record);

        assertEquals(
                GateDecision.ELIGIBLE,
                actual
        );
    }

    @Test
    @DisplayName("FAILED 상태인 기록은 10층이어도 참가 대상이 아니다")
    void failedRecordIsNotEligible() {
        RankingRecord record =
                validCandidate()
                        .status("FAILED")
                        .build();

        GateDecision actual =
                gate.evaluate(record);

        assertEquals(
                GateDecision.NOT_ELIGIBLE,
                actual
        );
    }

    @Test
    @DisplayName("10층에 도달하지 않은 기록은 CLEARED 상태여도 참가 대상이 아니다")
    void nonFinalFloorIsNotEligible() {
        RankingRecord record =
                validCandidate()
                        .clearedFloor(9)
                        .build();

        GateDecision actual =
                gate.evaluate(record);

        assertEquals(
                GateDecision.NOT_ELIGIBLE,
                actual
        );
    }
}