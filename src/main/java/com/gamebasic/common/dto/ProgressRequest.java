package com.gamebasic.common.dto;

import com.gamebasic.game.dto.RunCardRequest;
import com.gamebasic.game.entity.GamePhase;
import com.gamebasic.game.entity.GameStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ProgressRequest {

    @NotNull
    @Min(0)
    @Max(99)
    private Integer currnetHp;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer currentFloor;

    @NotNull
    private GamePhase phase;

    @NotNull
    private GameStatus status;

    @NotEmpty
    @Valid
    private List<RunCardRequest> deck;
}