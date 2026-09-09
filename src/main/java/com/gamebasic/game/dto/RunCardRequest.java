package com.gamebasic.game.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 요청에 포함 된 카드 정보 수신
@Getter
@NoArgsConstructor
public class RunCardRequest {

    @NotBlank
    private String cardType;

    @NotNull
    @Min(0)
    @Max(10)
    private Integer acquiredFloor;
}