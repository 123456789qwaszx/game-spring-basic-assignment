package com.gamebasic.ranking.core;

import java.util.List;

public final class BossPhaseInvariant implements RecordInvariant {

    private static final List<String> REQUIRED_PHASES =
            List.of(
                    "THRONE",
                    "UNBOUND",
                    "ECLIPSE"
            );

    @Override
    public RecordVerdict evaluate(RankingRecord record) {
        List<RankingRecord.BossFight.Phase> phases =
                record.bossFight().phases();

        if (phases.size() != REQUIRED_PHASES.size()) {
            return RecordVerdict.INVALID_BOSS_PHASES;
        }

        for (int index = 0; index < REQUIRED_PHASES.size(); index++) {
            String requiredPhase =
                    REQUIRED_PHASES.get(index);

            String actualPhase =
                    phases.get(index).phase();

            if (!requiredPhase.equals(actualPhase)) {
                return RecordVerdict.INVALID_BOSS_PHASES;
            }
        }

        return RecordVerdict.VALID;
    }
}