package com.gamebasic.game.dto;

import com.gamebasic.game.entity.GamePhase;
import com.gamebasic.game.entity.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

// 저장된 게임 상태 반환
@Getter
@AllArgsConstructor
public class GameDetailResponse {

    private Long id;
    private String playerName;
    private int currentHp;
    private int currentFloor;
    private GamePhase phase;
    private GameStatus status;
    private List<CardResponse> deck;
}