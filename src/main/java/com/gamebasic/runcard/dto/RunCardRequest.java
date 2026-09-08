package com.gamebasic.runcard.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class RunCardRequest {

    @NotBlank
    private String cardType;

    @NotNull
    @Min(0)
    @Max(10)
    private Integer acquiredFloor;
}
