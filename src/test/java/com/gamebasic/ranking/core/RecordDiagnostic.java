package com.gamebasic.ranking.core;

import java.util.List;

public class RecordDiagnostic {

    private final List<RecordInvariant> invariants;

    public RecordDiagnostic(List<RecordInvariant> invariants) {
        this.invariants = List.copyOf(invariants);
    }

    public RecordVerdict diagnose(RankingRecord record) {
        for (RecordInvariant invariant : invariants) {
            RecordVerdict verdict =
                    invariant.evaluate(record);

            if (verdict != RecordVerdict.VALID) {
                return verdict;
            }
        }

        return RecordVerdict.VALID;
    }
}