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

===

---- Lv8 ----

[1] 게임 회차에 저장된 이름 변경 및 삭제 기능 추가를 위한 설계 방향성 확인
- 클라 UI를 먼저 변경 후 서버 DB sync가 아님.
- 클라가 변경 요청 -> 서버 DB 실제 변경 -> 성공 후 목록 재조회 -> 데이터를 토대로 클라 UI 갱신.

1) 이름 교체 시
-> 서버 DB 변경
-> 204 No Content
-> 게임 목록 재조회
-> UI 갱신

2) 삭제 시
-> 서버에서 카드와 게임 삭제
-> 204 No Content
-> 게임 목록 재조회
-> 삭제된 게임이 UI에서 사라짐

[2] API 명세 확인

이름 변경:
PATCH /games/{gameId}

요청
```json
{
  "playerName": "붉은 순례자"
}
```
- 선택한 게임의 playerName만 DB에서 변경.
- 다른 진행 정보는 변경하지 않는다.
- 이름은 필수 / 공백 비허용 / 길이는 2자 이상 12자 이하.
- 변경 성공 시 '204 No Content'

게임 삭제:
DELETE /games/{gameId}

- 선택한 게임과 그 게임에 속한 모든 카드를 DB에서 삭제.
- 삭제 성공 시 '204 No Content'

[3] RenameRequest 작성
- CreateRequest의 playerName와 같은 명세.

[4] implement GameService.renameGame()

> 더티 체킹:
- renameGame()은 @Transactional이 적용된 상태.
- save가 없더라도, findGame은 gameRepository.findById()로 조회하기에,
- 반환된 Game은 
- 현재 트랜잭션의 영속성 컨텍스트에서 관리 및조회 시점의 값을 스냅샷으로 보관 중.
-
- 그 상태에서 game.rename(...)으로 필드를 바꾸면, 스냅샷과 현재 값이 달라짐.
- 트랙잭션이 커밋될 때, 하이버네이트가 flush를 수행, 관리 중인 엔티티들의 스냅샷과 현재값을 비교.
- 달라진 필드에 대해 UPDATE를 만들어 실행함.

[5] implement GameService.deleteGame()
- RunCard는 '@JoinColumn(name = "game_id", nullable = false)'로 games를 참조하는 FK를 가짐.
- 따라서 지우는 순서 '자식 - 부모'로 해야함.
- 동시에 '카드'가 없는 '게임'이라는 것이 없도록 '@Transactional' 애노테이션을 사용해 원자적으로 처리.

[6] add GameController.renameGame()
[7] add GameController.deleteGame()

[8] localhost:8080/ 브라우저에서 확인
- "저장된 여정"목록에서 이름 변경 진행.
- 삭제 후 목록에서 사라지는 것 확인.

- F12 -> Network -> games -> Headers
- Headers에서 요청 URL, HTTP 메서드, 상태 코드 확인
- 204 이후 즉각적으로 200 호출하여 갱신 됨을 확인.
Request URL
http://localhost:8080/games/6
Request Method
DELETE
Status Code
204 No Content
Remote Address
[::1]:8080
Referrer Policy
strict-origin-when-cross-origin

Request URL
http://localhost:8080/games
Request Method
GET
Status Code
200 OK
Remote Address
[::1]:8080
Referrer Policy
strict-origin-when-cross-origin

- DB에서도 삭제 됨.
```sql
mysql> SELECT * FROM games WHERE id = 6;
mysql> SELECT COUNT(*) FROM run_cards WHERE game_id = 6;
```
- 둘 다 결과 없음.
- GET /games/6이 404를 주는 것만으로도, 결과적으로 카드 삭제가 수행됨을 확인.
- (GET /games/6의 404 응답으로 Game 삭제 이후,
-  run_cards에서 game_id=6인 행의 개수가 0인 것으로 RunCard를 삭제 시킨 것임)

```sql
mysql> SELECT id, player_name, current_hp, current_floor, phase FROM games WHERE id = 9;

___Result___
9	변경 된 이름rf123	99	1	BATTLE
___
```
- DB상으로도 이름 변경 확인: '밤의 후계자(전)' -> '변경 된 이름rf123(후)'

===

---- lv9 ----

[1] 문제 확인
- 게임 클라 자체는 종료된 게임에 진행 저장 요청을 보내지 않음.
- 그럼에도 API를 직접 호출할 경우, 종료된 게임에 대한 PUT요청을 넣을 수 있음.
- 현재 서버는 그런 잘못된 요청도 다 받아줌.

- 게임의 상태 전이 규칙 자체를 서버가 소유하고 보장해야함.

[2] 기본 개념
- 클라를 신뢰 하지 않는다.
- 이번 경우 서버가 비즈니스 규칙을 검증한다.
- 현재 상태에 따라 가능한 요청 자체를 제한하고, 잘못된 변경은 데이터를 건드리기 전 차단한다.

[3] API 명세 확인
- CLEARED 또는 FAILED 상태의 게임은 변경할 수 없다.
- 종료된 게임에 진행 저장 요청(허용되지 않는 상태 변경 요청)을하면 409 Conflict를 반환한다.
- 요청을 거부한 경우에, 게임과 덱 데이터는 변경되지 않아야 한다.

[4] GameService.updateProgress에 검사 추가
- 위치: findGame 직후, game.updateProgress 실행 전.
- 실제 데이터 변경을 시작하기 전에 차단 및 의도 명시.
- 롤백은 사후수습. 이런 예외처리도 설계된 결과값.

[5] 테스트

1) FAILED 게임 추가

요청 
PUT localhost:8080/games/9/progress
```json
{
  "currentHp": 0,
  "currentFloor": 5,
  "phase": "FINISHED",
  "status": "FAILED",
  "deck": [
    { "cardType": "STRIKE", "acquiredFloor": 0 },
    { "cardType": "GUARD", "acquiredFloor": 0 }
  ]
}
```

응답 `200 OK`
```json
{
    "id": 9,
    "playerName": "변경 된 이름rf123",
    "currentHp": 0,
    "currentFloor": 5,
    "phase": "FINISHED",
    "status": "FAILED",
    "deck": [
        {
            "id": 143,
            "cardType": "STRIKE",
            "acquiredFloor": 0
        },
        {
            "id": 144,
            "cardType": "GUARD",
            "acquiredFloor": 0
        }
    ]
}
```

2) 현재 상태
```sql
mysql> SELECT id, player_name, current_hp, current_floor, phase, status FROM games WHERE id = 9;
___Result___
9	변경 된 이름rf123	0	5	FINISHED	FAILED
___	
```

```sql 
mysql> SELECT id, card_type, acquired_floor FROM run_cards WHERE game_id = 9 ORDER BY id;

___Result___
143	STRIKE	0
144	GUARD	0
___
```	

3) 재요청

요청 
PUT localhost:8080/games/9/progress
```json
{
  "currentHp": 99,
  "currentFloor": 1,
  "phase": "REWARD",
  "status": "PLAYING",
  "deck": [
    { "cardType": "MEND", "acquiredFloor": 3 }
  ]
}
```

응답 `409 Conflict`
```json
{
    "timestamp": "2026-09-08T16:52:57.079Z",
    "status": 409,
    "error": "Conflict",
    "path": "/games/9/progress"
}
```

4) DB 데이터 불변 확인

```sql 
mysql> SELECT id, player_name, current_hp, current_floor, phase, status FROM games WHERE id = 9;
mysql> SELECT id, card_type, acquired_floor FROM run_cards WHERE game_id = 9 ORDER BY id;
```
- 재요청 했음에도 데이터 변경 없음.
- game의 HP, 층, phase, status가 기존 값과 동일했다.
- RunCard의 id가 143, 144로 유지됐다.
- 의도한대로, 허용되지 않은 상태 변경 요청 차단을 확인.
- 브라우저에서도 의도한 대로 게임 동작 중.

===

---- lv10 ----

[1] 현재 상황
- 요청값 검증 실패인 400은 GlobalExceptionHandler가 처리 중.
- 게임을 찾지 못한 404와 종료된 게임의 변경을 막는 409는 Service에서 ResponseStatusException으로 처리 중.
- 400은 직접 정의한 ErrorResponse를 반환하지만, 404와 409는 Spring 기본 오류 응답을 반환.
- 404와 409에는 구체적인 실패 원인을 설명하는 message가 없다.

[2] 문제점
- 오류 처리가 Service와 전역 예외 처리기에 흩어져 있다.
- 400은 직접 정의한 응답을 사용하지만, 404와 409는 Spring 기본 오류 응답을 사용하여 형식이 일관되지 않다.
- 상태 코드만으로는 게임이 없는지, 종료된 게임인지 등 구체적인 실패 원인을 알기 어렵다.

[3] 개선 방안 및 목적
- 예외 처리와 응답 생성을 한곳에 모아, 모든 API가 일관된 형식과 명확한 message를 반환하도록 한다.
- 서버 내부에서 발생한 예외를 정해진 형식의 일관된 HTTP 응답으로 변환한다.
- 서로 다른 오류 원인을 하나의 일반적인 오류로 뭉개지 않는다.
- 클라이언트와 개발자가 실패 원인을 식별할 수 있는 오류 계약을 제공한다.

[4] GlobalExceptionHandler에 핸들러 추가
- GameNotFoundException(-> 404)과 GameFinishedException(-> 409)

- 반환방식 예시:
 return respond(HttpStatus.BAD_REQUEST, message, request);

- 공통 응답 생성 메서드:
private ResponseEntity<ErrorResponse> respond(
    HttpStatus status,
    String message,
    HttpServletRequest request
) {
    return ResponseEntity
        .status(status)
        .body(new ErrorResponse(
            status,
            message,
            request.getRequestURI()
        ));
}

- 즉 그냥 상태 코드랑 메시지만 결정 후 respond에 넘기면 끝.

- 각 예외가 가진 message를 e.getMessage()로.
- 기존 respond()를 재사용.

[5] GameService 예외 처리 경로 연결

>변경된 흐름(후):
1) GameService: 문제 발견 하고 예외 발생 처리.
-> GameFinishedException(gameId) 호출.

2)  public GameFinishedException(Long gameId) {
        super(
            "이미 끝난 여정은 진행을 저장할 수 없습니다. id="
            + gameId
        );
}
-> super()로 메시지를 부모에 전달.
-> 부모인 RuntimeException은 이 문자열을 예외 메시지로 보관.
(나중에 e.getMessage()로 꺼낼 수 있음)

3) Spring MVC가 발생한 예외를 확인하고 핸들러 매칭
- 예외가 Service → Controller → DispatcherServlet으로 전파됨
-> DispatcherServlet이 @RestControllerAdvice에 등록된 핸들러 중 예외 타입이 일치하는 것을 찾아 호출.
-> 핸들러 파라미터 타입을 보고 예외(e)와 HTTP 경로(HttpServletRequest)를 정리,
-> GlobalExceptionHandler.handleGameFinished(e, HttpServletRequest) 호출

4) handleGameFinished(e, HttpServletRequest)가 HTTP 응답(ErrorResponse)을 반환. 
-> respond()가 ErrorResponse와 요청 경로를 조립.

>기존 흐름(전):
1) GameService에서 HTTP 예외 발생
- throw new ResponseStatusException(HttpStatus.CONFLICT);
- 이미 서비스가 HTTP 상태 코드를 직접 지정했음. 메시지도 부재.

2) 예외 전파
- Service → Controller → DispatcherServlet.
- 하지만 @ExceptionHandler에는 매칭되지 않음.
-> Spring Boot의 기본 오류 처리 경로에서 처리됨.

> 전용 예외를 만든 이유:
1) 디버깅 편의
- 예외 타입만 보고도 어떤 문제인지 확인 가능.
- 로그와 Trace에서도 오류 종류가 명확하게 표시됨.

2) 계층 책임 명확.
- Service가 SpringWeb에 의존하지 않도록 함.
- ResponseStatusException를 사용하면 Service가 HttpStatus 받음.
- Service는 게임이 없거나 종료됐다는 것만 암. 예외 변환은 ExceptionHandler가 담당.

3) 응답 형식 통일
- 오류마다 JSON 구조가 달라지는 걸 방지.
- 가독성 상승과 더불어,
- 클라가 "Task<ApiResult<T>>" 등의 공통 오류 응답 래퍼와 역직렬화 코드 짜기 편해짐.

[6] 테스트

1) GET /games/999 - 존재하지 않는 게임 조회
- GameNotFoundException이 404 ErrorResponse로 변환되는지 확인.
- Spring 기본 응답의 timestamp가 사라지는지 확인.
- 예외가 가진 구체적인 message가 포함되는지 확인.

요청 
```http
GET localhost:8080/games/999
```

응답 `404 Not Found`
```json
{
    "status": 404,
    "message": "게임을 찾을 수 없습니다. id=999",
    "path": "/games/999",
    "error": "Not Found"
}
```
확인 결과:
- timestamp가 없고 message가 추가됨. 명세와 일치 확인.

2) PUT /games/9/progress - 종료된 게임 변경
- GameFinishedException이 409 ErrorResponse로 변환되는지 확인한다.
- 요청 본문은 검증을 통과할 수 있는 유효한 값으로 보낸다.

요청
```http
PUT localhost:8080/games/9/progress
```
```json
{
  "currentHp": 50,
  "currentFloor": 3,
  "phase": "BATTLE",
  "status": "PLAYING",
  "deck": [
    { "cardType": "STRIKE", "acquiredFloor": 0 }
  ]
}
```

응답 '409 Conflict'
```json
{
    "status": 409,
    "message": "이미 끝난 여정은 진행을 저장할 수 없습니다. id=9",
    "path": "/games/9/progress",
    "error": "Conflict"
}
```
확인 결과:
- 종료된 게임의 진행 변경 요청이 409를 반환.
- 404와 동일한 ErrorResponse 구조 확인.

3) PATCH /games/9 - 요청 본문 검증 400 회귀 테스트
- 기존 MethodArgumentNotValidException 핸들러 동작 확인
- 이름은 최소 2자이므로 한 글자 이름을 요청

요청
```http
PATCH localhost:8080/games/9
Content-Type: application/json
```
```json
{
    "playerName": "가" 
}
```

응답 '400 Bad Request'
```json
{
    "status": 400,
    "message": "playerName 값이 올바르지 않습니다: 크기가 2에서 12 사이여야 합니다",
    "path": "/games/9",
    "error": "Bad Request"
}
```
확인 결과: 
- 기존 MethodArgumentNotValidException 핸들러 정상 동작 확인.
- 400도 404, 409와 동일한 ErrorResponse 구조를 유지.

===

## M0

- 최소 GameService 및 GameController를 제외하고 삭제 완료.
- rewrite용 DB 추가.

- Java 21.0.12
- Gradle 9.5.1
- MySQL 8.0.46
- DB: game_rewrite

## M1


[1] Game과 RunCard를 JPA 엔티티로 만듬.

[2] 두 객체의 단뱡향 관계가 SQL테이블과 FK로 어떻게 표현되는지 확인.
- games.id: 게임 한 건을 식별하는 PK
- run_cards.id: 카드 한 장을 식별하는 PK
- run_cards.game_id: 카드가 어느 게임에 속하는지 나타내는 FK

[3] 어노테이션
1) @Entity
- 이 클래스가 JPA에서 관리하는 엔티티라는 것을 나타낸다.
- Hibernate는 엔티티의 필드와 어노테이션을 읽어 객체와 테이블을 연결한다. ddl-auto=update를 사용하는 현재 환경에서는 이 정보를 바탕으로 테이블과 컬럼도 갱신한다.

2) @Table(name = "games")
- 엔티티가 연결될 테이블 이름을 명시한다.
- @Table이 없을 때의 이름은 Hibernate의 네이밍 전략에 따라 결정되므로, 프로젝트에서 사용하는 테이블 이름을 명확하게 고정하기 위해 작성

3) @Index
- game_id에 인덱스를 두면 데이터가 많아졌을 때 전체 테이블을 순회하지 않고 해당 게임의 카드를 찾는 데 도움이 된다.

[4] 현재까지의 흐름을 정리하면
Java 필드: game
| @JoinColumn
DB 컬럼: game_id
| @Index
DB 인덱스: idx_run_card_game

@Index(
    name = "idx_run_card_game",
    columnList = "game_id"
)

>name = "idx_run_card_game": DB에 만들어 질 인덱스의 이름.  
(idx = index / run_card = RunCard 테이블 / game = game 기준 인덱스)

> columnList = "game_id"  
- 인덱스를 적용할 DB 컬럼 이름을 지정
- 여기에는 Java 필드 이름인 game이 아니라 실제 테이블의 컬럼 이름인 game_id를 적어야 함

[5] 테스트

1) 
```sql
mysql> USE game_rewrite;
mysql> SHOW TABLES;
```
___Result___
games
run_cards
___

2) 
```sql
mysql> DESC games;
```
___Result___
id	bigint	NO	PRI		auto_increment
current_floor	int	NO			
current_hp	int	NO			
phase	enum('BATTLE','FINISHED','REWARD')	NO			
player_name	varchar(12)	NO			
status	enum('CLEARED','FAILED','PLAYING')	NO			
___

3) 
```sql
mysql> DESC run_cards;;
```
___Result___
id	bigint	NO	PRI		auto_increment
acquired_floor	int	NO			
card_type	varchar(255)	NO			
game_id	bigint	NO	MUL
___

4) RunCard의 JPA 설정이 실제 DB에 반영 되었는지 확인
```sql
mysql> SHOW CREATE TABLE run_cards;
 ```
___Result___
run_cards	CREATE TABLE `run_cards` (
   `id` bigint NOT NULL AUTO_INCREMENT,
   `acquired_floor` int NOT NULL,
   `card_type` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
   `game_id` bigint NOT NULL,
   PRIMARY KEY (`id`),
   KEY `idx_run_card_game` (`game_id`),
   CONSTRAINT `FKp2ihkkwi75rttsbt7mptd9tkd` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
___

확인결과:
- `id`가 PK와 `AUTO_INCREMENT`로 생성됐다.
- `cardType` Enum은 `card_type varchar(255)`로 생성됐다.
- `game_id`는 `NOT NULL`로 생성됐다.
- `idx_run_card_game` 인덱스가 생성됐다.
- `game_id`가 `games.id`를 참조하는 FK로 생성됐다.


[6] Hibernate

지금 프로젝트의 층을 보면,
Spring Data JPA (: 레포의 인터페이스를 편하게 쓰게 해주는 층)
|
JPA (: 명세. @Entity, @Id, EntityManager 등을 정의)
|
Hibernate(: 그 명세의 구현체. 실제로 SQL을 만들고 실행)
|
JDBC -> MySQL

Spring Data JPA: Repository 인터페이스와 메서드 이름 기반 쿼리를 제공
JPA: @Entity, @Id, EntityManager 등의 표준을 정의
Hibernate: JPA 명세를 구현하고 실제 SQL을 생성·실행
JDBC: Java 애플리케이션과 MySQL 사이의 통신 담당
MySQL: 데이터를 실제로 저장

- 즉 Hibernate란, JPA의 명세를 토대로 실제 SQL을 실행하는 엔진.
- 영속성 컨텍스트는 JPA가 정의한 개념이고, Hibernate가 이를 실제로 구현
- 더티 체킹도 Hibernate가 제공하는 JPA 구현 기능이다. 트랜잭션 안에서 조회한 엔티티의 상태가 변경되면, 커밋 직전 flush 과정에서 기존 상태와 현재 상태를 비교해 필요한 UPDATE를 실행

## M2

[1] 요청 DTO 작성

1) RunCardRequest
@Getter
@NoArgsConstructor
public class RunCardRequest {

    @NotBlank
    private String cardType;

    @NotNull
    @Min(0)
    @Max(10)
    private Integer acquiredFloor;
}

2) CreateRequest
@Getter
@NoArgsConstructor
public class CreateRequest {

    @NotBlank
    @Size(min = 2, max = 12)
    private String playerName;

    @NotEmpty
    @Valid
    private List<RunCardRequest> deck;
}

[1-1] deck의 @Valid 사용 이유
- @Valid: 덱 내부의 각 RunCardRequest까지 검사.
- @NotEmpty: 덱 자체가 Null이거나 빈 배열인지 검사.

[1-2] acquiredFloor가 엔티티에서는 int, 요청 DTO에서는 Integer인 이유
- 만약 요청에서 필드가 빠졌을 때:
- int는 자동으로 0이 되어 누락을 구분할 수 없음.
- Integer는 null이 되므로 @NotNull로 누락을 검출할 수 있음.

[2] 응답 DTO 작성

[3] GameService 작성
@Transactional
public GameDetailResponse createGame(CreateRequest request) {
    Game game = gameRepository.save(
            new Game(request.getPlayerName()));

    saveDeck(game, request.getDeck());

    List<RunCard> cards =
            runCardRepository.findAllByGameOrderByIdAsc(game);

    return toDetailResponse(game, cards);
}

1) createRequest
2) Game 생성 및 INSERT
3) 생성된 Game을 각 RunCard의 FK로 사용
4) RunCard를 요청 순서대로 INSERT
5) 카드를 ID 오름 차순으로 다시 조회
6) GameDetailResponse 생성

[4] GameController 작성

@RestController
@RequestMapping("/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameDetailResponse> createGame(
            @Valid @RequestBody CreateRequest request
    ){
        GameDetailResponse response =
                gameService.createGame(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}

- @RequestMapping("/games")와 @PostMapping을 합치면 POST / games API가 됨.

- @RequestBody: 요청 JSON을 CreateRequest 객체로 변환
- @Valid까지 성공 시, gameService.createGame(request) 호출.

[5] GameRepository를 사용 가능한 이유
- 인터페이스 본문이 비어있음에도, JpaRepository로부터 이미 메서드를 상속 받음.
- <S extends Game> S save(S entity);
Optional<Game> findById(Lond id);
List<Game> findAll();
void delete(Game entity);
void deleteById(Long id)

- Game과의 연결은 인터페이스 본문이 아니라, extends 뒤의 제네릭 인자에서 이어짐.

게임을 보면, 
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
이를 Repository 선언과 대응시키면
JpaRepository<Game(관리할 엔티티), Long(Game.id의 타입)>

- 만약 save를 호출하면, JpaRepository<Game, Long>에서 Game을 알아낸 뒤
- JPA 설정을 읽음(@Entity, Table(name = "games"))
- 따라서 Hibernate가 개념적으로 아래 SQL을 실행.
```sql
INSERT INTO games (
    player_name,
    current_hp,
    current_floor,
    phase,
    status
)
VALUES (?, ?, ?, ?, ?);
```

[6] 테스트

1) PostMan 사용한 입력
요청
```http
Post localhost:8080/games
```
```json
{
  "playerName": "밤의 후계자",
  "deck": [
    {
      "cardType": "STRIKE",
      "acquiredFloor": 0
    },
    {
      "cardType": "GUARD",
      "acquiredFloor": 0
    }
  ]
}
```

응답 '201 Created'
```json
{
    "id": 1,
    "playerName": "밤의 후계자",
    "currentHp": 99,
    "currentFloor": 1,
    "phase": "REWARD",
    "status": "PLAYING",
    "deck": [
        {
            "id": 1,
            "cardType": "STRIKE",
            "acquiredFloor": 0
        },
        {
            "id": 2,
            "cardType": "GUARD",
            "acquiredFloor": 0
        }
    ]
}
```

2) MySql 확인
```sql
mysql> SELECT id, game_id, card_type, acquired_floor FROM run_cards ORDER BY id;
```
___Result___
1	1	STRIKE	0
2	1	GUARD	0
___
- DB 반영됨.

[7] validation 작성

1) Jackson(@RequestBody)이 JSON -> DTO 역직렬화
2) @Valid를 보고 Bean Validation(카드 제약) 실행
3) 위반한게 있으면 MethodArgumentNotValidException
4) 차후 GlobalExceptionHandler가 ErrorResponse로 변환
5) (통과 시에만) 메서드 본문 실행

[8] validation 테스트

1) 이름 글자 테스트
요청
```http
localhost:8080/games
```
```json
{
  "playerName": "밤",
  "deck": [
    {
      "cardType": "STRIKE",
      "acquiredFloor": 0
    }
  ]
}
```

응답 '400 Bad Request'
```json
{
    "status": 400,
    "message": "playerName값이 올바르지 않습니다: 크기가 2에서 12 사이여야 합니다",
    "path": "/games",
    "error": "Bad Request"
}
```

2) 카드 내부 공백 검증
요청
```http
localhost:8080/games
```
```json
{
  "playerName": "밤의 후계자",
  "deck": [
    {
      "cardType": "   ",
      "acquiredFloor": 11
    }
  ]
}
```

응답 '400 Bad Request'
```json
{
    "status": 400,
    "message": "deck[0].cardType값이 올바르지 않습니다: 공백일 수 없습니다",
    "path": "/games",
    "error": "Bad Request"
}

확인 결과:
- deck의 @Valid 연결 확인

## M3

## M4

## M5

## M6

## M7