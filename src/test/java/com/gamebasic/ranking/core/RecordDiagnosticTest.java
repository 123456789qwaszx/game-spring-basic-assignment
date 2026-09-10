package com.gamebasic.ranking.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordDiagnosticTest {

    private RankingGate gate;
    private RecordDiagnostic diagnostic;

    @BeforeEach
    void setUp() {
        gate = new RankingGate();

        diagnostic = new RecordDiagnostic(
                List.of(new ClearTimeInvariant())
        );
    }

    @Test
    @DisplayName("10층을 300초에 클리어한 기록은 시간 조건을 통과한다")
    void minimumClearTimeIsValid() {
        RankingRecord record = candidate(300);

        GateDecision gateDecision =
                gate.evaluate(record);

        RecordVerdict verdict =
                diagnostic.diagnose(record);

        assertEquals(
                GateDecision.ELIGIBLE,
                gateDecision
        );

        assertEquals(
                RecordVerdict.VALID,
                verdict
        );
    }

    @Test
    @DisplayName("10층을 299초에 클리어한 기록은 시작 조건을 위반한다")
    void fasterThanMinimumIsInvalid() {
        RankingRecord record = candidate(299);

        GateDecision gateDecision =
                gate.evaluate(record);

        RecordVerdict verdict =
                diagnostic.diagnose(record);

        assertEquals(
                GateDecision.ELIGIBLE,
                gateDecision
        );

        assertEquals(
                RecordVerdict.INVALID_DURATION,
                verdict
        );
    }

    @Test
    @DisplayName("최소 시간보다 오래 걸린 기록은 시간 조건을 통과한다")
    void slowerThanMinimumIsValid() {
        RankingRecord record = candidate(301);

        RecordVerdict verdict =
                diagnostic.diagnose(record);

        assertEquals(
                RecordVerdict.VALID,
                verdict
        );
    }

    private RankingRecord candidate(
            Integer durationSeconds
    ) {
        return new RankingRecord(
                "CLEARED",
                10,
                durationSeconds
        );
    }
}