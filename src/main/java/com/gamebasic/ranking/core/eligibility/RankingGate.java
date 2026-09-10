package com.gamebasic.ranking.core.eligibility;

import com.gamebasic.ranking.core.model.RankingRecord;

public final class RankingGate {

    public GateDecision evaluate(RankingRecord record){
        boolean isCleared =
                "CLEARED".equals(record.status());

        boolean reachedFinalFloor =
                Integer.valueOf(10).equals(record.clearedFloor());

        if(isCleared && reachedFinalFloor){
            return GateDecision.ELIGIBLE;
        }

        return GateDecision.NOT_ELIGIBLE;
    }
}