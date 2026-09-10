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
    @DisplayName("정상 기록을 시간, HP, 기록 ID 우선순위로 정렬한다")
    void validRecordsAreSortedByRankingOrder() {
        // 시간이 가장 짧아서 1순위
        RankingRecord fastestRecord =
                validCandidate()
                        .recordId(50L)
                        .durationSeconds(300)
                        .finalHp(1)
                        .build();

        // 같은 500초 기록 중 HP가 가장 높아서 우선
        RankingRecord highHpRecord =
                validCandidate()
                        .recordId(20L)
                        .durationSeconds(500)
                        .finalHp(90)
                        .build();

        // 시간과 HP가 같으므로 ID 2가 ID 10보다 우선
        RankingRecord lowerIdRecord =
                validCandidate()
                        .recordId(2L)
                        .durationSeconds(500)
                        .finalHp(50)
                        .build();

        RankingRecord higherIdRecord =
                validCandidate()
                        .recordId(10L)
                        .durationSeconds(500)
                        .finalHp(50)
                        .build();

        // HP가 높아도 시간이 느리므로 마지막
        RankingRecord slowestRecord =
                validCandidate()
                        .recordId(1L)
                        .durationSeconds(700)
                        .finalHp(99)
                        .build();

        RankingPolicy policy =
                policyWith(candidate -> VALID);

        RankingScreeningResult result =
                policy.screen(
                        List.of(
                                higherIdRecord,
                                slowestRecord,
                                highHpRecord,
                                fastestRecord,
                                lowerIdRecord
                        )
                );

        assertEquals(
                List.of(
                        fastestRecord,
                        highHpRecord,
                        lowerIdRecord,
                        higherIdRecord,
                        slowestRecord
                ),
                result.validRecords()
        );
    }

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