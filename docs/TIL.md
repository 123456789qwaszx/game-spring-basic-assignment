# 학습노트 — 직접 채우는 TIL

실행 결과는 M#-check에 남기고 여기에는 내 설명과 실제 경험을 쓴다.

## 발제 과제

---- Lv1 ----

[1] Docker 설치.

[2] docker --version
Docker version 29.7.2, build a7dcaa6

[3] src/main/resources/application.properties 추가 완료

[4] PS docker run --name game_c -e MYSQL_ROOT_PASSWORD=1234 -e MYSQL_DATABASE=game_c -p 3307:3306 -d mysql:8.0

[5] application.properties 작성
spring.datasource.url=jdbc:mysql://localhost:3307/game_c
spring.datasource.username=root(sql 계정)
spring.datasource.password=1234

[6] Docker 컨테이너와 application.properties 대조

[7] application.properties에 spring.jpa.hibernate.ddl-auto=update 추가
- 테이블 자동 생성 및 재시작 후 데이터 유지.

[8] PS ./gradlew clean bootRun 실행
->
에러메시지 출력:
 "required a bean of type 'com.gamebasic.game.service.GameService' that could not be found"

문제:
1) 스프링 컨테이너에 그 타입의 객체가 없음.
2) GameService 에 문제가 있음.

====

---- Lv2 ----

[1] 오류 메시지 원인:
- Lombok이 GameController(GameService gameService) 생성자를 만듬
- 스프링은 컨트롤러 객체를 만들려고 생성자를 호출
- 파라미터에 넣을 GameService bean이 컨테이너에 없기에 생성 실패.

[2] GameService에 @Service 애노테이션을 추가하여 스캔 대상 추가 및 빈 등록.

[3] PS ./gradlew bootRun 성공
-> Started GameBasicApplication 2.317 seconds (process running for 2.534)

[4] SQL 테이블 생성 확인.

```sql
mysql> USE game_c;
mysql> SHOW TABLES;

___Result___
games
run_cards
___
```

```sql
mysql> DESC games;
mysql> DESC run_cards;
```

```sql
mysql> SELECT * FROM games;
mysql> SELECT * FROM run_cards;
```

[5] http://localhost:8080/를 통해 게임 실행 확인
-> 게임 진행 불가. "저장 목록을 불러오지 못했습니다. 서버 연결을 확인하세요."

===

---- Lv3 ----

[1] 게임 목록 조회 - 수정 전

요청
```http
GET http://localhost:8080/games
```

응답 `405 Method Not Allowed`
```json
{
    "timestamp": "2026-09-08T08:45:44.812Z",
    "status": 405,
    "error": "Method Not Allowed",
    "path": "/games"
}
```

> 404 = 경로에 매핑된 게 없음 /  
405 = 경로는 존재하지만, 요청한 HTTP 메서드에 대한 매핑이 없음.

[2] API 명세와 Controller 매핑 비교

- 명세: GET /games
- Controller 확인 결과
@GetMapping("/game")
    public ResponseEntity<List<Object>> getGames() {
        // List<Object>는 임시 구현이며, Lv 7에서 제대로 고칩니다.
        // List.of()는 빈 목록을 돌려주는 임시 구현이며, Lv 7에서 제대로 고칩니다.
        return ResponseEntity.ok(List.of());
    }
- "/game" 오탈자 확인 및 수정 -> "/games"

[3] 게임 목록 조회 — 수정 후

요청
```http
GET http://localhost:8080/games
```

응답 `200 OK`
```json
[]
```

[4] http://localhost:8080/를 통해 게임 실행 확인
-> 에러 수정 완료, 하지만 추가 에러 메시지 확인.
-> '게임 시작'을 누르면,
   (Log: "요청을 처리하지 못했습니다.") 출력 및 게임 진행 불가

===

---- lv4 ----

[1] 에러 재현
- 서버 실행 후 http://localhost:8080/ 접속 -> '게임 시작' -> 이름 입력 -> '새 게임' 클릭

요청
```http
POST http://localhost:8080/games
Content-Type: application/json
```
```json
{
  "playerName": "밤의 후계자",
  "deck": [
    {
      "cardType": "STRIKE",
      "acquiredFloor": 0
    },
    ...
    {
      "cardType": "MEND",
      "acquiredFloor": 0
    }
  ]
}
```

응답 `500 Internal Server Error`
```json
{
    "timestamp": "2026-09-08T10:29:55.838Z",
    "status": 500,
    "error": "Internal Server Error",
    "path": "/games"
}
```

[2] 원인: 서버 내부에서 예외가 처리되지 않음. 서버 로그 확인

서버 로그
```
at com.gamebasic.game.service.GameService.createGame(GameService.java:30)
at com.gamebasic.game.service.GameService$$SpringCGLIB$$0.createGame(<generated>)
at com.gamebasic.game.controller.GameController.createGame(GameController.java:31)
...
~
WARN  ... org.hibernate.orm.jdbc.error : HHH000247: ErrorCode: 0, SQLState: S1009
WARN  ... org.hibernate.orm.jdbc.error : Connection is read-only. Queries leading to data modification are not allowed
ERROR ... [dispatcherServlet] : ... JpaSystemException: could not execute statement [Connection is read-only...] 
```

- 데이터 베이스 연결은 읽기 전용임에도, INSERT/UPDATE/DELETE 같은 쓰기 쿼리를 실행하려고 함.
- gamebasic.game.service.GameService.createGame 확인 필요.

[3] GameService.createGame 확인

@Transactional(readOnly = true)
    public GameDetailResponse createGame(CreateRequest request) {
        Game game = gameRepository.save(new Game(request.getPlayerName()));
        saveDeck(game, request.getDeck());
        List<RunCard> cards = runCardRepository.findAllByGameOrderByIdAsc(game);
        List<CardResponse> deck = new ArrayList<>();
        for (RunCard card : cards) {
            deck.add(new CardResponse(card.getId(), card.getCardType(), card.getAcquiredFloor()));
        }
        return new GameDetailResponse(
            game.getId(),
            game.getPlayerName(),
            game.getCurrentHp(),
            game.getCurrentFloor(),
            game.getPhase(),
            game.getStatus(),
            deck
        );
    }

- 단순 조회가 아닌, 게임 초기값 세팅과 시작 덱 생성 후 서버 저장 등의 쓰기가 필요한 메서드임에도 readonly가 설정되어 있음.
- (readOnly = true)를 (readOnly = false)로 변경

[4] http://localhost:8080/를 통해 게임 실행 확인
-> 에러 수정 완료, 하지만 추가 에러 메시지 확인.
-> '게임 시작'을 누르면,
   (Log: "서버 응답이 API 명세와 다릅니다 (deck[0].id)") 출력 및 게임 진행 불가

===

---- Lv5 ----

[1] CardResponse(응답 DTO)와 RunCardRequest(요청 DTO) 확인
- 아직 미구현 된 stub 임.

[2] 기획에 따른 API 명세 확인

>RunCardRequest
- cardType
  - 필수
  - null, 빈 문자열, 공백만 있는 문자열을 허용하지 않음
  - 서버에서 enum으로 제한하지 않고 문자열로 저장
- acquiredFloor
  - 필수
  - 최소 0
  - 최대 10

[3] RunCardRequest, CardResponse 구현
- RunCardRequest는 클라측이 보낸 카드 정보를 수신.
- CardResponse는 저장된 카드 정보를 클라에 반환.

- 카드 id는 데이터베이스 저장 과정에서 생성. 요청x 응답에만 o

[4] http://localhost:8080/를 통해 게임 실행 확인
-> 게임 시작 확인, 시작 보상 화면까지 도달. 
-> 하지만 '보상 카드'를 누르면,
   (Log: "저장된 게임을 찾을 수 없습니다. 저장 목록으로 돌아갑니다.") 출력 및 타이틀로 돌아감.
   게임 진행 불가.

===

---- Lv6 ----

[1] DB 확인

```sql
mysql> SELECT id, player_name, current_hp, current_floor, phase, status FROM games;

___Result___
5	밤의 후계자	99	1	REWARD	PLAYING
6	밤의 후계자	99	1	REWARD	PLAYING
___
```
- 로그와 다르게 저장은 되는 중.

[2] '보상 카드'를 누르는 흐름 추적
- 클라가 덱 전체(시작 9장 + 고른 1장)을 PUT/games/{gamdId}/progress 로 보냄
- 스프링은 games/{gamdId}/progress 경로에서 어떤 것도 찾지 못해서 404 반환.
- 이전 Lv3의 '405'의 경우 매핑은 있되, GET이 없어서 405 였었음.
- 반면 현재는 경로자체가 부재하여 404를 반환하는 중.

[3] Controller에 PutMapping 추가

[4] http://localhost:8080/를 통해 게임 실행 확인
-> 정상 진행 확인.
- 하지만 DB에 저장이 되었음에도 UI 상 Load 목록 리스트에 표시되지 않는 현상 확인.

===

---- Lv7 ----

[1] 현재 상태 확인
- F12 -> Network -> games -> Headers
- Headers에서 요청 URL, HTTP 메서드, 상태 코드 확인
- Response에서 응답 본문 `[]` 확인

요청
```http
GET http://localhost:8080/games
```

응답 `200 OK`
```json
[]
```
- 클라는 페이지 로드 시 목록 API를 호출 하지만 빈 배열만 받는 중.
- DB에도 게임이 저장되어 있음.
- 클라 측 UI 화면 표시도 이상 없음.

- 임시 서버 측 조회 API가 빈 배열 반환 중.

[2] API 계약 확인

>GET /games
- 모든 게임 조회
- 게임 id 기준 내림차순
- 덱은 포함하지 않음
- 결과가 없으면 []

>GET /games/{gameId}
- 특정 게임 상세 조회
- 전체 덱 포함
- 덱은 RunCard.id 기준 오름차순
- 게임이 없으면 404

[3] GameSummaryResponse 작성
- 게임 목록 API의 응답 계약을 표현하는 DTO.
- Game 엔티티에서 목록에 필요한 필드만 선택하여 반환.
- 상세 응답과 달리 덱은 포함하지 않음.

[4] GameRepository 작성
- 조건없는 전체 조회.
- List<Game> 반환.
- Game.id 기준 내림차순.

>List<Game> findAllByOrderByIdDesc();
- findAll -> 전체 조회
- By      -> 조회 조건 표현의 시작
- OrderBy -> 정렬한다
- Id      -> Game의 id필드를 기준으로
- Desc    -> 내림차순으로

- Spring Data JPA가 이름을 분석하여 실행 코드 자동 생성.
- 선언만 하더라도 개념적으로 아래와 같은 쿼리가 만들어짐.
```
SELECT *
FROM games
ORDER BY id DESC;
```

[5] implement GameService.getGames()
- 모든 게임 조회.
- 덱이 없는 게임 요약 목록

- 반환값: List<GameSummaryResponse>
- 즉 Game엔티티를 DTO로 변환해야함.

[6] implement GameService.getGame(Long)
- 특정 게임 회차 하나 조회.
- 덱을 포함한 게임 상세

[7] implement GameController.getGames()

[8] add GameController.getGame(@PathVariable Long gameId)

[9] 테스트
1) GET /games
  - 200 OK 확인.
  - 게임 ID가 7, 6, 5 순서이므로 내림차순 정렬 확인.
  - 목록 응답에 deck이 포함되지 않는 것을 확인.
요청
```http
GET http://localhost:8080/games
```

응답 `200 OK`
```json
[
    {
        "id": 7,
        "playerName": "밤의 후계자",
        "currentFloor": 1,
        "currentHp": 99,
        "phase": "BATTLE",
        "status": "PLAYING"
    },
    {
        "id": 6,
        "playerName": "밤의 후계자",
        "currentFloor": 1,
        "currentHp": 99,
        "phase": "REWARD",
        "status": "PLAYING"
    },
    {
        "id": 5,
        "playerName": "밤의 후계자",
        "currentFloor": 1,
        "currentHp": 99,
        "phase": "REWARD",
        "status": "PLAYING"
    }
]
```

2) GET /games/5
  - 200 OK 확인.
  - 게임 상세 정보와 전체 덱 반환 확인.
  - 카드 ID가 68부터 76까지 오름차순인 것을 확인.
요청
```http
GET http://localhost:8080/games/5
```

응답 `200 OK`
```json
{
    "id": 5,
    "playerName": "밤의 후계자",
    "currentHp": 99,
    "currentFloor": 1,
    "phase": "REWARD",
    "status": "PLAYING",
    "deck": [
        {
            "id": 68,
            "cardType": "STRIKE",
            "acquiredFloor": 0
        },
        {
            "id": 69,
            "cardType": "STRIKE",
            "acquiredFloor": 0
        },
        {
            "id": 70,
            "cardType": "HEART_PIERCE",
            "acquiredFloor": 0
        },
        {
            "id": 71,
            "cardType": "GUARD",
            "acquiredFloor": 0
        },
        {
            "id": 72,
            "cardType": "MIST_KNOT",
            "acquiredFloor": 0
        },
        {
            "id": 73,
            "cardType": "QUICK_SLASH",
            "acquiredFloor": 0
        },
        {
            "id": 74,
            "cardType": "WARDING_SLASH",
            "acquiredFloor": 0
        },
        {
            "id": 75,
            "cardType": "BLOOD_RUNE",
            "acquiredFloor": 0
        },
        {
            "id": 76,
            "cardType": "MEND",
            "acquiredFloor": 0
        }
    ]
}
```

3) GET /games/999
  - 존재하지 않는 게임에 대해 404 확인.
  - 현재는 Spring 기본 오류 응답이며, Lv10에서 명세 형식으로 변경할 예정.
요청
```http
GET http://localhost:8080/games/999
```

응답 `404 Not Found`
```json
{
    "timestamp": "2026-09-08T14:17:52.947Z",
    "status": 404,
    "error": "Not Found",
    "path": "/games/999"
}
```

4) GET /games/abc
  - Long으로 변환할 수 없는 gameId에 대해 400 확인.
  - 요청이 Controller 메서드에 전달되기 전 PathVariable 변환 단계에서 실패.
요청
```http
GET http://localhost:8080/games/abc
```

응답 `400 Bad Request`
```json
{
    "status": 400,
    "message": "요청 본문이나 파라미터 형식이 올바르지 않습니다.",
    "path": "/games/abc",
    "error": "Bad Request"
}
```

[10] http://localhost:8080/를 통해 게임 실행 확인
- 타이틀에 "저장된 여정" 버튼 추가 됨.
- "저장된 여정"에 게임 목록 표시 확인 완료
- 선택 시 게임 로드 완료.
- 저장된 HP, 층 수, 덱 목록 DB와 일치.

## M0

## M1

## M2

## M3

## M4

## M5

## M6

## M7