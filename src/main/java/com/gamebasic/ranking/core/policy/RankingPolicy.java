package com.gamebasic.ranking.core.policy;

import com.gamebasic.ranking.core.eligibility.GateDecision;
import com.gamebasic.ranking.core.eligibility.RankingGate;
import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.core.validation.RecordDiagnostic;
import com.gamebasic.ranking.core.validation.RecordVerdict;

import java.util.ArrayList;
import java.util.List;

public class RankingPolicy {

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

        return new RankingScreeningResult(
                validRecords,
                excludedCount
        );
    }
}
