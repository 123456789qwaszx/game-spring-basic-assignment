package com.gamebasic.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RenameRequest {

    @NotBlank
    @Size(min = 2, max = 12)
    private String playerName;
}