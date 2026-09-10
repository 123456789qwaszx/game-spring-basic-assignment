package com.gamebasic.ranking.core.policy;

import com.gamebasic.ranking.core.eligibility.RankingGate;
import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.core.validation.RecordDiagnostic;
import com.gamebasic.ranking.core.validation.invariant.ClearTimeInvariant;
import com.gamebasic.ranking.core.validation.invariant.FinalHpInvariant;
import com.gamebasic.ranking.core.validation.invariant.RecordInvariant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.gamebasic.ranking.core.validation.RecordVerdict.INVALID_HP;
import static com.gamebasic.ranking.core.validation.RecordVerdict.VALID;
import static com.gamebasic.ranking.testfixture.RankingRecordFixture.validCandidate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankingPolicyTest {

    @Test
    @DisplayName("참가 대상이며 정상인 기록은 정상 목록에 포함한다")
    void eligibleValidRecordIsIncluded() {
        RankingRecord record =
                validCandidate().build();

        RankingPolicy policy =
                policyWith(
                        candidate -> VALID
                );

        RankingScreeningResult result =
                policy.screen(List.of(record));

        assertEquals(
                List.of(record),
                result.validRecords()
        );

        assertEquals(
                0,
                result.excludedCount()
        );
    }

    @Test
    @DisplayName("참가 대상이 아닌 기록은 제외 수에 포함하지 않는다")
    void ineligibleRecordIsNotCountedAsExcluded() {
        RankingRecord record =
                validCandidate()
                        .status("FAILED")
                        .build();

        RankingPolicy policy =
                policyWith(
                        candidate -> VALID
                );

        RankingScreeningResult result =
                policy.screen(List.of(record));

        assertTrue(
                result.validRecords().isEmpty()
        );

        assertEquals(
                0,
                result.excludedCount()
        );
    }

    @Test
    @DisplayName("참가 대상이지만 비정상인 기록은 제외 수에 포함한다")
    void eligibleInvalidRecordIsCountedAsExcluded() {
        RankingRecord record =
                validCandidate().build();

        RankingPolicy policy =
                policyWith(
                        candidate -> INVALID_HP
                );

        RankingScreeningResult result =
                policy.screen(List.of(record));

        assertTrue(
                result.validRecords().isEmpty()
        );

        assertEquals(
                1,
                result.excludedCount()
        );
    }

    @Test
    @DisplayName("여러 정상 조건을 위반한 기록도 한 건으로 집계한다")
    void recordViolatingMultipleConditionsIsCountedOnce() {
        RankingRecord record =
                validCandidate()
                        .durationSeconds(299)
                        .finalHp(0)
                        .build();

        RankingPolicy policy =
                policyWith(
                        new ClearTimeInvariant(),
                        new FinalHpInvariant()
                );

        RankingScreeningResult result =
                policy.screen(List.of(record));

        assertTrue(
                result.validRecords().isEmpty()
        );

        assertEquals(
                1,
                result.excludedCount()
        );
    }

    private RankingPolicy policyWith(
            RecordInvariant... invariants
    ) {
        return new RankingPolicy(
                new RankingGate(),
                new RecordDiagnostic(
                        List.of(invariants)
                )
        );
    }
}