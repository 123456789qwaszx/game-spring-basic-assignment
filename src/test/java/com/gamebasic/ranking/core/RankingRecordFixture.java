package com.gamebasic.ranking.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

final class RankingRecordFixture {

    static final String VALID_STATUS = "CLEARED";
    static final int VALID_CLEARED_FLOOR = 10;
    static final int VALID_DURATION_SECONDS = 300;
    static final int VALID_FINAL_HP = 99;
    static final int VALID_DECK_SIZE = 9;

    static final RankingRecord.Deck VALID_DECK =
            new RankingRecord.Deck(
                    VALID_DECK_SIZE,
                    List.of(
                            card("STRIKE", 0),
                            card("STRIKE", 0),
                            card("HEART_PIERCE", 0),
                            card("GUARD", 0),
                            card("MIST_KNOT", 0),
                            card("QUICK_SLASH", 0),
                            card("WARDING_SLASH", 0),
                            card("BLOOD_RUNE", 0),
                            card("MEND", 0)
                    )
            );

    static final Set<String> ALLOWED_CARD_TYPES =
            Arrays.stream(CardType.values())
                    .map(Enum::name)
                    .collect(Collectors.toUnmodifiableSet());

    static final int VALID_PHASE_TURNS = 1;
    static final int VALID_DAMAGE_TAKEN = 0;
    static final String VALID_FINISHING_CARD = "STRIKE";
    static final int VALID_BOSS_TOTAL_TURNS = 3;

    static final RankingRecord.BossFight VALID_BOSS_FIGHT =
            new RankingRecord.BossFight(
                    List.of(
                            phase(
                                    "THRONE",
                                    VALID_PHASE_TURNS,
                                    VALID_DAMAGE_TAKEN
                            ),
                            phase(
                                    "UNBOUND",
                                    VALID_PHASE_TURNS,
                                    VALID_DAMAGE_TAKEN
                            ),
                            phase(
                                    "ECLIPSE",
                                    VALID_PHASE_TURNS,
                                    VALID_DAMAGE_TAKEN
                            )
                    ),
                    VALID_FINISHING_CARD,
                    VALID_BOSS_TOTAL_TURNS
            );

    private RankingRecordFixture() {
    }

    static Builder validCandidate() {
        return new Builder();
    }

    static RankingRecord.Deck deck(
            Integer declaredSize,
            int actualSize
    ) {
        List<RankingRecord.Deck.Card> cards =
                IntStream.range(0, actualSize)
                        .mapToObj(index ->
                                card("STRIKE", 0)
                        )
                        .toList();

        return new RankingRecord.Deck(
                declaredSize,
                cards
        );
    }

    static RankingRecord.Deck.Card card(
            String cardType,
            Integer acquiredFloor
    ) {
        return new RankingRecord.Deck.Card(
                cardType,
                acquiredFloor
        );
    }

    static RankingRecord.Deck deckContaining(
            RankingRecord.Deck.Card changedCard
    ) {
        List<RankingRecord.Deck.Card> cards =
                new ArrayList<>(VALID_DECK.cards());

        cards.set(0, changedCard);

        return new RankingRecord.Deck(
                cards.size(),
                cards
        );
    }

    static RankingRecord.BossFight.Phase phase(
            String phase,
            Integer turns,
            Integer damageTaken
    ) {
        return new RankingRecord.BossFight.Phase(
                phase,
                turns,
                damageTaken
        );
    }

    static RankingRecord.BossFight bossFightWithPhases(
            List<RankingRecord.BossFight.Phase> phases
    ) {
        int totalTurns =
                phases.stream()
                        .mapToInt(RankingRecord.BossFight.Phase::turns)
                        .sum();

        return new RankingRecord.BossFight(
                phases,
                VALID_FINISHING_CARD,
                totalTurns
        );
    }

    static RankingRecord.BossFight bossFightWithTurns(
            int throneTurns,
            int unboundTurns,
            int eclipseTurns
    ) {
        return bossFightWithPhases(
                List.of(
                        phase(
                                "THRONE",
                                throneTurns,
                                VALID_DAMAGE_TAKEN
                        ),
                        phase(
                                "UNBOUND",
                                unboundTurns,
                                VALID_DAMAGE_TAKEN
                        ),
                        phase(
                                "ECLIPSE",
                                eclipseTurns,
                                VALID_DAMAGE_TAKEN
                        )
                )
        );
    }

    static final class Builder {

        private String status = VALID_STATUS;
        private Integer clearedFloor = VALID_CLEARED_FLOOR;
        private Integer durationSeconds = VALID_DURATION_SECONDS;
        private Integer finalHp = VALID_FINAL_HP;
        private RankingRecord.Deck deck = VALID_DECK;

        private RankingRecord.BossFight bossFight = VALID_BOSS_FIGHT;

        Builder status(String status) {
            this.status = status;
            return this;
        }

        Builder clearedFloor(Integer clearedFloor) {
            this.clearedFloor = clearedFloor;
            return this;
        }

        Builder durationSeconds(Integer durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        Builder finalHp(Integer finalHp) {
            this.finalHp = finalHp;
            return this;
        }

        Builder deck(RankingRecord.Deck deck) {
            this.deck = deck;
            return this;
        }

        Builder bossFight(
                RankingRecord.BossFight bossFight
        ) {
            this.bossFight = bossFight;
            return this;
        }

        RankingRecord build() {
            return new RankingRecord(
                    status,
                    clearedFloor,
                    durationSeconds,
                    finalHp,
                    deck,
                    bossFight
            );
        }
    }
}