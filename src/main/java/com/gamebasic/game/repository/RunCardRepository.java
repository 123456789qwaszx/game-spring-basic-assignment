package com.gamebasic.game.repository;

import com.gamebasic.game.dto.DeckSizeResponse;
import com.gamebasic.game.entity.Game;
import com.gamebasic.game.entity.RunCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RunCardRepository extends JpaRepository<RunCard, Long> {

    // SELECT *
    // FROM run_cards
    // WHERE game_id = ?
    // ORDER BY id ASC;
    List<RunCard> findAllByGameOrderByIdAsc(Game game);

    // DELETE
    // FROM run_cards
    // WHERE game_id = ?;
    void deleteAllByGame(Game game);

    @Query("""
            SELECT new com.gamebasic.game.dto.DeckSizeResponse(
            card.game.id,
            COUNT(card)
            )
            FROM RunCard card
            WHERE card.game.id IN :gameIds
            GROUP BY card.game.id
            """
    )
    List<DeckSizeResponse> findDeckSizesByGameIds(
            @Param("gameIds") List<Long> gameIds
    );
}