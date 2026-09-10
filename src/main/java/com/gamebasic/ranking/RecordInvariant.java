package com.gamebasic.ranking;

import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.core.validation.RecordVerdict;

public interface RecordInvariant {

    RecordVerdict evaluate(RankingRecord record);
}
