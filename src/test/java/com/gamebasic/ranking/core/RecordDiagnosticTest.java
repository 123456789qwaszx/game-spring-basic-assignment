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
                List.of(new ClearTimeInvariant(),
                        new FinalHpInvariant()
                )
        );
    }

    // ---- 남은 HP 조건 ----
    @Test
    @DisplayName("남은 HP가 1이면 HP 조건을 통과한다")
    void minimumHpIsValid() {
        RankingRecord record =
                candidate(300, 1);

        RecordVerdict verdict =
                diagnostic.diagnose(record);

        assertEquals(
                RecordVerdict.VALID,
                verdict
        );
    }

    @Test
    @DisplayName("남은 HP가 99이면 HP 조건을 통과한다")
    void maximumHpIsValid() {
        RankingRecord record =
                candidate(300, 99);

        RecordVerdict verdict =
                diagnostic.diagnose(record);

        assertEquals(
                RecordVerdict.VALID,
                verdict
        );
    }

    @Test
    @DisplayName("남은 HP가 100이면 HP조건을 위반한다")
    void zeroHpIsInvalid() {
        RankingRecord record =
                candidate(300, 0);

        RecordVerdict verdict =
                diagnostic.diagnose(record);

        assertEquals(
                RecordVerdict.INVALID_HP,
                verdict
        );
    }

    @Test
    @DisplayName("남은 HP가 100이면 HP 조건을 위반한다")
    void overMaximumHpIsInvalid() {
        RankingRecord record =
                candidate(300, 100);

        RecordVerdict verdict =
                diagnostic.diagnose(record);

        assertEquals(
                RecordVerdict.INVALID_HP,
                verdict
        );
    }


    // ---- 클리어 시간 조건 ----

    @Test
    @DisplayName("10층을 300초에 클리어한 기록은 시간 조건을 통과한다")
    void minimumClearTimeIsValid() {
        RankingRecord record = candidate(300, 99);

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
        RankingRecord record = candidate(299, 99);

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
        RankingRecord record = candidate(301, 99);

        RecordVerdict verdict =
                diagnostic.diagnose(record);

        assertEquals(
                RecordVerdict.VALID,
                verdict
        );
    }

    private RankingRecord candidate(
            Integer durationSeconds,
            Integer finalHp
    ) {
        return new RankingRecord(
                "CLEARED",
                10,
                durationSeconds,
                finalHp
        );
    }
}