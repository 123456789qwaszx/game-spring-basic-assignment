package com.gamebasic.ranking.service;

import com.gamebasic.ranking.client.RankingClient;
import com.gamebasic.ranking.client.RankingSource;
import com.gamebasic.ranking.core.eligibility.RankingGate;
import com.gamebasic.ranking.core.model.CardType;
import com.gamebasic.ranking.core.model.RankingRecord;
import com.gamebasic.ranking.core.policy.RankingPolicy;
import com.gamebasic.ranking.core.policy.RankingScreeningResult;
import com.gamebasic.ranking.core.validation.RecordDiagnostic;
import com.gamebasic.ranking.core.validation.invariant.AcquiredFloorInvariant;
import com.gamebasic.ranking.core.validation.invariant.BossPhaseInvariant;
import com.gamebasic.ranking.core.validation.invariant.BossPhaseTurnsInvariant;
import com.gamebasic.ranking.core.validation.invariant.BossTotalTurnsInvariant;
import com.gamebasic.ranking.core.validation.invariant.CardTypeInvariant;
import com.gamebasic.ranking.core.validation.invariant.ClearTimeInvariant;
import com.gamebasic.ranking.core.validation.invariant.DeckSizeInvariant;
import com.gamebasic.ranking.core.validation.invariant.FinalHpInvariant;
import com.gamebasic.ranking.core.validation.invariant.FinishingCardInvariant;
import com.gamebasic.ranking.dto.RankingEntryResponse;
import com.gamebasic.ranking.dto.RankingResponse;
import com.gamebasic.ranking.mapper.RankingSourceMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RankingService {

    private final RankingClient rankingClient;
    private final RankingSourceMapper rankingSourceMapper;
    private final RankingPolicy rankingPolicy;

    public RankingService(
            RankingClient rankingClient,
            RankingSourceMapper rankingSourceMapper
    ) {
        this.rankingClient = rankingClient;
        this.rankingSourceMapper = rankingSourceMapper;
        this.rankingPolicy = createRankingPolicy();
    }

    public RankingResponse getRankings() {
        RankingSource source =
                rankingClient.fetch();

        List<RankingRecord> records =
                rankingSourceMapper.mapRecords(source);

        RankingScreeningResult result =
                rankingPolicy.screen(records);

        List<RankingEntryResponse> entries =
                createEntries(result.validRecords());

        return new RankingResponse(
                source.getMeta()
                        .getSeason()
                        .getId(),
                source.getRecords().size(),
                result.excludedCount(),
                entries
        );
    }

    private List<RankingEntryResponse> createEntries(
            List<RankingRecord> records
    ) {
        List<RankingEntryResponse> entries = new ArrayList<>();

        for (int index = 0; index < records.size(); index++) {
            RankingRecord record =
                    records.get(index);

            entries.add(
                    new RankingEntryResponse(
                            index + 1,
                            record.player().name(),
                            record.durationSeconds(),
                            record.finalHp(),
                            record.bossFight().totalTurns(),
                            record.deck().cards().size()
                    )
            );
        }

        return entries;
    }

    private static RankingPolicy createRankingPolicy() {
        Set<String> allowedCardTypes =
                Arrays.stream(CardType.values())
                        .map(Enum::name)
                        .collect(Collectors.toUnmodifiableSet());

        RankingGate gate = new RankingGate();

        RecordDiagnostic diagnostic =
                new RecordDiagnostic(
                        List.of(
                                new ClearTimeInvariant(),
                                new FinalHpInvariant(),
                                new DeckSizeInvariant(),
                                new CardTypeInvariant(allowedCardTypes),
                                new AcquiredFloorInvariant(),
                                new BossPhaseInvariant(),
                                new BossPhaseTurnsInvariant(),
                                new BossTotalTurnsInvariant(),
                                new FinishingCardInvariant()
                        )
                );

        return new RankingPolicy(gate, diagnostic);
    }
}