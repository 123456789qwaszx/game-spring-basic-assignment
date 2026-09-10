package com.gamebasic.ranking.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gamebasic.ranking.core.RankingRecordFixture.ALLOWED_CARD_TYPES;
import static com.gamebasic.ranking.core.RankingRecordFixture.validCandidate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordDiagnosticTest {

    @Test
    @DisplayName("등록된 모든 정상 조건을 통과하면 VALID를 반환한다")
    void allInvariantsPass() {
        RecordDiagnostic diagnostic =
                new RecordDiagnostic(
                        List.of(
                                new ClearTimeInvariant(),
                                new FinalHpInvariant(),
                                new DeckSizeInvariant(),
                                new CardTypeInvariant(ALLOWED_CARD_TYPES),
                                new AcquiredFloorInvariant(),
                                new BossPhaseInvariant(),
                                new BossPhaseTurnsInvariant()
                        )
                );

        RankingRecord record =
                validCandidate().build();

        RecordVerdict verdict =
                diagnostic.diagnose(record);

        assertEquals(
                RecordVerdict.VALID,
                verdict
        );
    }
}