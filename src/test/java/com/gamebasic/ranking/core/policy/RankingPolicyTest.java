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

    // 입력 순서가 아니라 정렬 결과를 기준으로 최고 기록을 고른다.
    // 같은 player.id에서는 하나만 유지한다.
    // 중복으로 빠진 정상 기록은 excludedCount에 포함하지 않는다.
    // 플레이어 이름이 같아도 player.id가 다르면 서로 다른 플레이어다.
    @Test
    @DisplayName("플레이어별로 정렬 순서상 가장 좋은 정상 기록 하나만 유지한다")
    void bestRecordPerPlayerIsKept() {
        RankingRecord inferiorRecord =
                validCandidate()
                        .recordId(1L)
                        .player("player-a", "같은 이름")
                        .durationSeconds(500)
                        .finalHp(90)
                        .build();

        RankingRecord sameNameDifferentPlayer =
                validCandidate()
                        .recordId(2L)
                        .player("player-b", "같은 이름")
                        .durationSeconds(450)
                        .finalHp(50)
                        .build();

        RankingRecord bestRecord =
                validCandidate()
                        .recordId(3L)
                        .player("player-a", "같은 이름")
                        .durationSeconds(400)
                        .finalHp(30)
                        .build();

        RankingPolicy policy =
                policyWith(
                        candidate -> VALID
                );

        RankingScreeningResult result =
                policy.screen(
                        List.of(
                                inferiorRecord,
                                sameNameDifferentPlayer,
                                bestRecord
                        )
                );

        assertEquals(
                List.of(
                        bestRecord,
                        sameNameDifferentPlayer
                ),
                result.validRecords()
        );

        assertEquals(
                0,
                result.excludedCount()
        );
    }

    @Test
    @DisplayName("정상 기록을 시간, HP, 기록 ID 우선순위로 정렬한다")
    void validRecordsAreSortedByRankingOrder() {
        // 시간이 가장 짧아서 1순위
        RankingRecord fastestRecord =
                validCandidate()
                        .recordId(50L)
                        .player("player-fastest", "가장 빠른 플레이어")
                        .durationSeconds(300)
                        .finalHp(1)
                        .build();

        // 같은 500초 기록 중 HP가 가장 높아서 우선
        RankingRecord highHpRecord =
                validCandidate()
                        .recordId(20L)
                        .player("player-high-hp", "HP가 높은 플레이어")
                        .durationSeconds(500)
                        .finalHp(90)
                        .build();

        // 시간과 HP가 같으므로 ID 2가 ID 10보다 우선
        RankingRecord lowerIdRecord =
                validCandidate()
                        .recordId(2L)
                        .player("player-lower-id", "낮은 ID 플레이어")
                        .durationSeconds(500)
                        .finalHp(50)
                        .build();

        RankingRecord higherIdRecord =
                validCandidate()
                        .recordId(10L)
                        .player("player-higher-id", "높은 ID 플레이어")
                        .durationSeconds(500)
                        .finalHp(50)
                        .build();

        // HP가 높아도 시간이 느리므로 마지막
        RankingRecord slowestRecord =
                validCandidate()
                        .recordId(1L)
                        .player("player-slowest", "가장 느린 플레이어")
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