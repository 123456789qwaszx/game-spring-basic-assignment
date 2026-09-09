package com.gamebasic.game.repository;

import com.gamebasic.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 관리할 Entity: Game
// Game의 ID 타입 : Long
// - 이 인터페이스를 직접 구현하지 않아도,
//   Spring Data JPA가 런타임에 구현 객체를 만들어 Bean으로 등록함.
public interface GameRepository extends JpaRepository<Game, Long> {

    // findAll: 모든 Game 조회
    // By: 조회 조건 영역 구분
    // 조건 없음
    // OrderByIdDesc: id 기준 내림차순 정렬
    List<Game> findAllByOrderByIdDesc();
}