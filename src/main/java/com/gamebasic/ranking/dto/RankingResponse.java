package com.gamebasic.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class RankingResponse {

    private String season;
    private int totalRecords;
    private int excludedCount;
    private List<RankingEntryResponse> entries;
}