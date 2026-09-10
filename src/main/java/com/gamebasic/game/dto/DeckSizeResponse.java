package com.gamebasic.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// API 응답이 아닌 JPQL 집계 결과를 받기 위한 내부용 DTO
@Getter
@AllArgsConstructor
public class DeckSizeResponse {

    private Long gameId;
    private Long deckSize;
}