package com.gamebasic.game.service;

import com.gamebasic.common.dto.RenameRequest;
import com.gamebasic.common.exception.GameFinishedException;
import com.gamebasic.common.exception.GameNotFoundException;
import com.gamebasic.game.dto.CreateRequest;
import com.gamebasic.game.dto.GameDetailResponse;
import com.gamebasic.game.dto.GameSummaryResponse;
import com.gamebasic.game.dto.ProgressRequest;
import com.gamebasic.game.entity.Game;
import com.gamebasic.game.repository.GameRepository;
import com.gamebasic.runcard.dto.CardResponse;
import com.gamebasic.runcard.dto.RunCardRequest;
import com.gamebasic.runcard.entity.RunCard;
import com.gamebasic.runcard.repository.RunCardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

public class GameService {

}