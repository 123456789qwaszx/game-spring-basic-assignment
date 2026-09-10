package com.gamebasic.ranking.core;

public record RankingRecord (
        String status,
        Integer clearedFloor,
        Integer durationSeconds,
        Integer finalHp
){
}