package com.gamebasic.game.controller;

import com.gamebasic.common.dto.ProgressRequest;
import com.gamebasic.common.dto.RenameRequest;
import com.gamebasic.game.dto.CreateRequest;
import com.gamebasic.game.dto.GameDetailResponse;
import com.gamebasic.game.dto.GameSummaryResponse;
import com.gamebasic.game.service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameDetailResponse> createGame(
            @Valid @RequestBody CreateRequest request
    ){
        GameDetailResponse response =
                gameService.createGame(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<GameDetailResponse> getGame(
            @PathVariable Long gameId
    ){
        return ResponseEntity.ok(
                gameService.getGame(gameId)
        );
    }

    @PutMapping("/{gameId}/progress")
    public ResponseEntity<GameDetailResponse> updateProgress(
            @PathVariable Long gameId,
            @Valid @RequestBody ProgressRequest request
    ){
        return ResponseEntity.ok(
                gameService.updateProgress(gameId, request)
        );
    }

    @GetMapping
    public ResponseEntity<List<GameSummaryResponse>> getGames() {
        return ResponseEntity.ok(
                gameService.getGames()
        );
    }

    @PatchMapping("/{gameId}")
    public ResponseEntity<Void> renameGame(
            @PathVariable Long gameId,
            @Valid @RequestBody RenameRequest request
    ){
        gameService.renameGame(gameId, request);

        return ResponseEntity.noContent().build();
    }
}