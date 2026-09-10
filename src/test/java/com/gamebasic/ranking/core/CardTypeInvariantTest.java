package com.gamebasic.ranking.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.gamebasic.ranking.core.RankingRecordFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CardTypeInvariantTest {

    private CardTypeInvariant invariant;

    @BeforeEach
    void setUP() {
        invariant = new CardTypeInvariant(
                ALLOWED_CARD_TYPES
        );
    }

    @Test
    @DisplayName("모든 카드 타입이 허용 목록에 있으면 통과한다")
    void allowedCardTypesAreValid() {
        RankingRecord record =
                validCandidate().build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.VALID,
                verdict
        );
    }

    @Test
    @DisplayName("허용 목록에 없는 카드 타입이 하나라도 있으면 위반한다")
    void unknownCardTypeIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .deck(
                                deckContaining(
                                        card("CHEAT_CARD", 0)
                                )
                        )
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_CARD_TYPE,
                verdict
        );
    }

    @Test
    @DisplayName("카드 타입의 대소문자가 다르면 허용 타입으로 인정하지 않는다")
    void lowercaseCardTypeIsInvalid() {
        RankingRecord record =
                validCandidate()
                        .deck(
                                deckContaining(
                                        card("strike", 0)
                                )
                        )
                        .build();

        RecordVerdict verdict =
                invariant.evaluate(record);

        assertEquals(
                RecordVerdict.INVALID_CARD_TYPE,
                verdict
        );
    }
}