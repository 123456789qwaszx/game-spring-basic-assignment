package com.gamebasic.ranking.core.validation;

import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.core.validation.invariant.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gamebasic.ranking.testfixture.RankingRecordFixture.ALLOWED_CARD_TYPES;
import static com.gamebasic.ranking.testfixture.RankingRecordFixture.validCandidate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
                                new BossPhaseTurnsInvariant(),
                                new BossTotalTurnsInvariant(),
                                new FinishingCardInvariant()
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

    @Test
    @DisplayName("첫 번째 위반을 반환하고 이후 정상 조건은 검사하지 않는다")
    void returnsFirstViolationAndStopsEvaluation() {
        RecordDiagnostic diagnostic =
                new RecordDiagnostic(
                        List.of(
                                record -> RecordVerdict.VALID,
                                record -> RecordVerdict.INVALID_HP,
                                record -> {
                                    fail("첫 번째 위반 이후의 정상 조건이 실행되었습니다.");
                                    return RecordVerdict.VALID;
                                }
                        )
                );

        RankingRecord record =
                validCandidate().build();

        RecordVerdict verdict =
                diagnostic.diagnose(record);

        assertEquals(
                RecordVerdict.INVALID_HP,
                verdict
        );
    }
}