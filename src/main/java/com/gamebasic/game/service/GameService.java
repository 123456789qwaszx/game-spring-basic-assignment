package com.gamebasic.game.service;

import com.gamebasic.common.dto.ProgressRequest;
import com.gamebasic.game.dto.*;
import com.gamebasic.game.entity.Game;
import com.gamebasic.game.entity.RunCard;
import com.gamebasic.game.repository.GameRepository;
import com.gamebasic.game.repository.RunCardRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final RunCardRepository runCardRepository;

    public GameService(
            GameRepository gameRepository,
            RunCardRepository runCardRepository
    ){
        this.gameRepository = gameRepository;
        this.runCardRepository = runCardRepository;
    }

    @Transactional
    public GameDetailResponse updateProgress(
            Long gameId,
            ProgressRequest request
    ){
        Game game = findGame(gameId);

        game.updateProgress(
                request.getCurrentHp(),
                request.getCurrentFloor(),
                request.getPhase(),
                request.getStatus()
        );

        runCardRepository.deleteAllByGame(game);

        saveDeck(game, request.getDeck());

        List<RunCard> cards =
                runCardRepository.findAllByGameOrderByIdAsc(game);

        return toDetailResponse(game, cards);
    }

    @Transactional
    public GameDetailResponse createGame(CreateRequest request) {
        Game game = gameRepository.save(
                new Game(request.getPlayerName()));

        saveDeck(game, request.getDeck());

        List<RunCard> cards =
                runCardRepository.findAllByGameOrderByIdAsc(game);

        return toDetailResponse(game, cards);
    }

    @Transactional(readOnly = true)
    public GameDetailResponse getGame(Long gameId) {
        Game game = findGame(gameId);

        List<RunCard> cards =
                runCardRepository.findAllByGameOrderByIdAsc(game);

        return toDetailResponse(game, cards);
    }

    @Transactional(readOnly = true)
    public List<GameSummaryResponse> getGames() {
        List<Game> games =
                gameRepository.findAllByOrderByIdDesc();

        List<GameSummaryResponse> response =
                new ArrayList<>();

        for (Game game : games){
            response.add(new GameSummaryResponse(
                    game.getId(),
                    game.getPlayerName(),
                    game.getCurrentHp(),
                    game.getCurrentFloor(),
                    game.getPhase(),
                    game.getStatus()
            ));
        }

        return response;
    }

    private void saveDeck(
            Game game,
            List<RunCardRequest> deck
    ){
        for (RunCardRequest cardRequest : deck){
            RunCard card = new RunCard(
                    game,
                    cardRequest.getCardType(),
                    cardRequest.getAcquiredFloor()
            );

            runCardRepository.save(card);
        }
    }

    private GameDetailResponse toDetailResponse(
            Game game,
            List<RunCard> cards
    ){
        List<CardResponse> deck = new ArrayList<>();

        for(RunCard card : cards){
            deck.add(new CardResponse(
                    card.getId(),
                    card.getCardType(),
                    card.getAcquiredFloor()
            ));
        }

        return new GameDetailResponse(
                game.getId(),
                game.getPlayerName(),
                game.getCurrentHp(),
                game.getCurrentFloor(),
                game.getPhase(),
                game.getStatus(),
                deck
        );
    }

    private Game findGame(Long gameId){
        return gameRepository.findById(gameId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND)
                );
    }
}