package com.gamebasic.game.dto;

import com.gamebasic.game.entity.GamePhase;
import com.gamebasic.game.entity.GameStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class GameSummaryResponse {

    private Long id;
    private String playerName;
    private int currentHp;
    private int currentFloor;
    private GamePhase phase;
    private GameStatus status;

    private long deckSize;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}