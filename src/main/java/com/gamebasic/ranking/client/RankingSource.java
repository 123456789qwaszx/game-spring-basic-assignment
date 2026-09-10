package com.gamebasic.ranking.client;

import lombok.Getter;

import java.util.List;

@Getter
public class RankingSource {

    private Meta meta;
    private List<SourceRecord> records;

    @Getter
    public static class Meta {

        private Season season;
    }

    @Getter
    public static class Season {

        private String id;
    }

    @Getter
    public static class SourceRecord {

        private Long id;
        private Player player;
        private Run run;
        private BossFight bossFight;
        private Deck deck;
    }

    @Getter
    public static class Player {

        private String id;
        private String name;
    }

    @Getter
    public static class Run {

        private String status;
        private Integer clearedFloor;
        private Integer durationSeconds;
        private Integer finalHp;
    }

    @Getter
    public static class BossFight {

        private List<Phase> phases;
        private String finishingCard;
        private Integer totalTurns;
    }

    @Getter
    public static class Phase {

        private String phase;
        private Integer turns;
        private Integer damageTaken;
    }

    @Getter
    public static class Deck {

        private Integer size;
        private List<Card> cards;
    }

    @Getter
    public static class Card {

        private String cardType;
        private Integer acquiredFloor;
    }
}