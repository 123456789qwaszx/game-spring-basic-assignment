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

## M0

## M1

## M2

## M3

## M4

## M5

## M6

## M7