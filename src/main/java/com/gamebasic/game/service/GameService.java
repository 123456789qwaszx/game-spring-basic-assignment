package com.gamebasic.game.service;

import com.gamebasic.game.dto.CardResponse;
import com.gamebasic.game.dto.CreateRequest;
import com.gamebasic.game.dto.GameDetailResponse;
import com.gamebasic.game.dto.RunCardRequest;
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
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND)
                );

        List<RunCard> cards =
                runCardRepository.findAllByGameOrderByIdAsc(game);

        return toDetailResponse(game, cards);
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
}