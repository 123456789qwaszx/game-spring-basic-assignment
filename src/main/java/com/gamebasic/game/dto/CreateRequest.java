package com.gamebasic.game.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

// 클라가 보낸 생성 요청 수신
@Getter
@NoArgsConstructor
public class CreateRequest {

    @NotBlank
    @Size(min = 2, max = 12)
    private String playerName;

    @NotEmpty
    @Valid
    private List<RunCardRequest> deck;
}