package com.gamebasic.ranking.core.policy;

import com.gamebasic.ranking.core.eligibility.GateDecision;
import com.gamebasic.ranking.core.eligibility.RankingGate;
import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.core.validation.RecordDiagnostic;
import com.gamebasic.ranking.core.validation.RecordVerdict;

import java.util.*;

public class RankingPolicy {

    private static final Comparator<RankingRecord> RANKING_ORDER =
            Comparator
                    .comparingInt(// 숫자가 작은 기록을 앞으로.
                            (RankingRecord record) ->
                                    record.durationSeconds()
                    )
                    .thenComparing(// 기본 숫자는 오름 차순. HP 비교에만 reversed()
                            Comparator
                                    .comparingInt(
                                            (RankingRecord record) ->
                                                    record.finalHp()
                                    )
                                    .reversed()
                    )
                    .thenComparingLong(// 시간과 HP가 같다면 ID 낮은 값을 앞으로.
                            record ->
                                    record.recordId()
                    );

    private final RankingGate gate;
    private final RecordDiagnostic diagnostic;

    public RankingPolicy(
            RankingGate gate,
            RecordDiagnostic diagnostic
    ) {
        this.gate = gate;
        this.diagnostic = diagnostic;
    }

    public RankingScreeningResult screen(
            List<RankingRecord> records
    ) {
        List<RankingRecord> validRecords = new ArrayList<>();

        int excludedCount = 0;

        for (RankingRecord record : records) {
            GateDecision gateDecision =
                    gate.evaluate(record);

            if(gateDecision == GateDecision.NOT_ELIGIBLE) {
                continue;
            }

            RecordVerdict verdict =
                    diagnostic.diagnose(record);

            if(verdict == RecordVerdict.VALID) {
                validRecords.add(record);
            } else {
                excludedCount++;
            }
        }

        validRecords.sort(RANKING_ORDER);

        List<RankingRecord> bestRecords =
                selectBestRecordPerPlayer(validRecords);

        return new RankingScreeningResult(
                bestRecords,
                excludedCount
        );
    }

    private List<RankingRecord> selectBestRecordPerPlayer(
            List<RankingRecord> sortedRecords
    ) {
        Set<String> seenPlayerIds = new HashSet<>();

        List<RankingRecord> bestRecords = new ArrayList<>();

        for (RankingRecord record : sortedRecords) {
            String playerId =
                    record.player().id();

            if (seenPlayerIds.add(playerId)) {
                bestRecords.add(record);
            }
        }

        return bestRecords;
    }
}