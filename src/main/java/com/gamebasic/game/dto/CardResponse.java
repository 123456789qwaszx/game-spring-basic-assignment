package com.gamebasic.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

// DB에 저장된 카드 반환
@Getter
@AllArgsConstructor
public class CardResponse {

    private Long id;
    private String cardType;
    private int acquiredFloor;
}