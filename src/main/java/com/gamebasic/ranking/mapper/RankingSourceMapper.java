package com.gamebasic.ranking.mapper;

import com.gamebasic.ranking.client.RankingSource;
import com.gamebasic.ranking.core.model.RankingRecord;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public final class RankingSourceMapper {

    public List<RankingRecord> mapRecords(
            RankingSource source
    ) {
        return source.getRecords()
                .stream()
                .map(this::mapRecord)
                .toList();
    }

    private RankingRecord mapRecord(
            RankingSource.SourceRecord source
    ) {
        RankingSource.Run run =
                source.getRun();

        return new RankingRecord(
                source.getId(),
                mapPlayer(source.getPlayer()),
                run.getStatus(),
                run.getClearedFloor(),
                run.getDurationSeconds(),
                run.getFinalHp(),
                mapDeck(source.getDeck()),
                mapBossFight(source.getBossFight())
        );
    }

    private RankingRecord.Player mapPlayer(
            RankingSource.Player source
    ) {
        return new RankingRecord.Player(
                source.getId(),
                source.getName()
        );
    }

    private RankingRecord.Deck mapDeck(
            RankingSource.Deck source
    ) {
        List<RankingRecord.Deck.Card> cards =
                source.getCards()
                        .stream()
                        .map(this::mapCard)
                        .toList();

        return new RankingRecord.Deck(
                source.getSize(),
                cards
        );
    }

    private RankingRecord.Deck.Card mapCard(
            RankingSource.Card source
    ) {
        return new RankingRecord.Deck.Card(
                source.getCardType(),
                source.getAcquiredFloor()
        );
    }

    private RankingRecord.BossFight mapBossFight(
            RankingSource.BossFight source
    ) {
        if (source == null) {
            return null;
        }

        List<RankingRecord.BossFight.Phase> phases =
                source.getPhases()
                        .stream()
                        .map(this::mapPhase)
                        .toList();

        return new RankingRecord.BossFight(
                phases,
                source.getFinishingCard(),
                source.getTotalTurns()
        );
    }

    private RankingRecord.BossFight.Phase mapPhase(
            RankingSource.Phase source
    ) {
        return new RankingRecord.BossFight.Phase(
                source.getPhase(),
                source.getTurns(),
                source.getDamageTaken()
        );
    }
}