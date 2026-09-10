package com.gamebasic.ranking.core;

public final class FinalHpInvariant implements RecordInvariant {

    private static final int MINIMUM_HP = 1;
    private static final int MAXIMUM_HP = 99;

    @Override
    public RecordVerdict evaluate(RankingRecord record) {
        boolean isInRange =
                record.finalHp() >= MINIMUM_HP
                && record.finalHp() <= MAXIMUM_HP;

        if (!isInRange) {
            return RecordVerdict.INVALID_HP;
        }

        return RecordVerdict.VALID;
    }
}
