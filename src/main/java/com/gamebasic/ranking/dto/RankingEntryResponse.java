package com.gamebasic.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// entries[] 한 항목.
@Getter
@AllArgsConstructor
public class RankingEntryResponse {

    private int rank;
    private String playerName;
    private int clearTimeSeconds;
    private int remainingHp;
    private int bossTurns;
    private int deckSize;
}