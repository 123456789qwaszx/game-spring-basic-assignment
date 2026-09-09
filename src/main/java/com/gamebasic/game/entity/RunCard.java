package com.gamebasic.game.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "run_cards",
        indexes = @Index(
                name = "idx_run_card_game",
                columnList = "game_id"
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RunCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private String cardType;

    @Column(nullable = false)
    private int acquiredFloor;

    public RunCard(
            Game game,
            String cardType,
            int acquiredFloor
    ){
        this.game = game;
        this.cardType = cardType;
        this.acquiredFloor = acquiredFloor;
    }
}