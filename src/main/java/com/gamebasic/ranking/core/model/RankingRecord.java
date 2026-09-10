package com.gamebasic.ranking.core.model;

import java.util.List;

public record RankingRecord(
        Long recordId,
        Player player,
        String status,
        Integer clearedFloor,
        Integer durationSeconds,
        Integer finalHp,
        Deck deck,
        BossFight bossFight
) {

    public record Player(
            String id,
            String name
    ) {
    }

    public record Deck(
            Integer declaredSize,
            List<Card> cards
    ) {
        public Deck {
            cards = List.copyOf(cards);
        }

        public record Card(
                String cardType,
                Integer acquiredFloor
        ) {
        }
    }

    public record BossFight(
            List<Phase> phases,
            String finishingCard,
            Integer totalTurns
    ) {
        public BossFight {
            phases = List.copyOf(phases);
        }

        public record Phase(
                String phase,
                Integer turns,
                Integer damageTaken
        ) {
        }
    }
}