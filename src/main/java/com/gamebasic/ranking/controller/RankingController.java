package com.gamebasic.ranking.controller;

import com.gamebasic.ranking.dto.RankingResponse;
import com.gamebasic.ranking.service.RankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RankingController {

    private final RankingService rankingService;

    public RankingController(
            RankingService rankingService
    ) {
        this.rankingService = rankingService;
    }

    @GetMapping("/rankings")
    public ResponseEntity<RankingResponse> getRankings() {
        return ResponseEntity.ok(
                rankingService.getRankings()
        );
    }
}