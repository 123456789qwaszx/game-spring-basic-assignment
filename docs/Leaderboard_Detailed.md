# Ranking 설계도

## 정상 기록 판정

### 중요도
- 금전적 가치 없고,
- 순위에 따른 재화 지급도 없고,
- 업적도 없고,
- 공개 경쟁이지만,   

-> 오락실에서 친구끼리 점수 비교하는 정도

### 클라와 기획의 설계
- 서버 권위 아님
- 전투 계산, 전 배치, 보상, 층 이동 등은 클라
- 기존 저장 서버는 그저 게임과 덱을 저장 / 조회만 담당

- 외부 랭킹 API를 통해서 시즌 전체 제출 기록을 받아옴.

->
(1) 행동 단위 로그가 없어, 개별 행동과 전체 상태 전이를 재현, 검증할 수 없음.

(2) 층별 로그가 있으므로 일부 변화, 모순은 대조 가능. 다만 이번 명세의 검사 대상에는 포함되지 않음.

(3) 최종 결과와 층별, 보스전 요약 로그를 받음. 따라서 기록된 페이즈 순서와 턴 합계의 일관성을 확인 가능.

- 정상 범위 검사
- 참가 자격 확인(CLEARED && 10층)
- 10-1, 10-2, 10-3(보스전)은 페이즈 순서 검사

### 현재 수신하는 데이터
// 시즌 및 응답 메타 정보
00. meta
01. meta.season
02. meta.season.id
03. meta.season.name
04. meta.season.startsAt
05. meta.season.endsAt
06. meta.generatedAt
07. meta.schemaVersion
08. meta.totalRecords

// 제출 기록
10. records[]
11. records[].id
12. records[].submittedAt

// 클라이언트 정보
20. client
21. client.version
22. client.platform
23. client.locale

// 플레이어 정보
30. player
31. player.id
32. player.name
33. player.region
34. player.tags[]

// 플레이 결과
40. run
41. run.seed
42. run.status
43. run.clearedFloor
44. run.durationSeconds
45. run.finalHp

// 층별 로그
50. run.floors[]
51. run.floors[].floor
52. run.floors[].enemy
53. run.floors[].turns
54. run.floors[].hpAfter

// 보상 로그
60. run.floors[].rewards[]
61. run.floors[].rewards[].offered[]
62. run.floors[].rewards[].picked

// 보스전 로그
70. bossFight
71. bossFight.phases[]
72. bossFight.phases[].phase
73. bossFight.phases[].turns
74. bossFight.phases[].damageTaken
75. bossFight.totalTurns
76. bossFight.finishingCard

// 최종 덱
80. deck
81. deck.size
82. deck.cards[]
83. deck.cards[].cardType
84. deck.cards[].acquiredFloor


## 명세 요구 판정
[1] 참가 자격
1) 42. run.status == CLEARED
2) 43. run.clearedFloor == 10

-> 두 조건을 모두 만족해야 순위 대상.
-> 미충족 기록은 순위와 excludedCount 모두에서 제외.

[2] 클리어 시간
1) 44. run.durationSeconds >= 43. run.clearedFloor * 30

-> 순위 대상은 10층이므로 300초 이상.

[3] 남은 HP
1) 45. run.finalHp >= 1
2) 45. run.finalHp <= 99

[4] 덱 크기
1) 82. deck.cards[]의 실제 개수 >= 9
2) 82. deck.cards[]의 실제 개수 <= 20
3) 81. deck.size == 82. deck.cards[]의 실제 개수

[5] 카드 타입
1) 83. 모든 deck.cards[].cardType이 허용된 카드 타입 38종에 포함

[6] 카드 획득 층
1) 84. 모든 deck.cards[].acquiredFloor >= 0
2) 84. 모든 deck.cards[].acquiredFloor <= 9

[7] 보스 페이즈
1) 71. bossFight.phases[]의 실제 개수 == 3
2) 72. bossFight.phases[0].phase == THRONE
3) 72. bossFight.phases[1].phase == UNBOUND
4) 72. bossFight.phases[2].phase == ECLIPSE

-> 원본 배열의 순서대로 검사.

[8] 보스 페이즈별 턴 수
1) 73. 모든 bossFight.phases[].turns >= 1

[9] 보스 총 턴 수
1) 75. bossFight.totalTurns == 73. bossFight.phases[].turns의 합계

[10] 마무리 카드
1) 76. bossFight.finishingCard가 83. deck.cards[].cardType 중 하나와 일치

-> 해당 기록의 최종 덱에 포함되어야 함.

### 랭킹 집계 및 순위 산정
[900] 이상 기록 제외 및 집계
1) [1] 참가 자격을 충족한 기록만 정상 여부 검사
2) [2]-[10] 중 하나라도 위반하면 해당 기록 제외
3) 제외한 기록 수를 excludedCount로 집계

-> 한 기록이 여러 조건을 위반해도 1건으로 계산.

[901] 정상 기록 정렬
1) 44. run.durationSeconds 오름차순
2) 시간이 같으면 45. run.finalHp 내림차순
3) 시간과 HP가 같으면 11. records[].id 숫자 오름차순

[902] 플레이어별 최고 기록 선택
1) 31. player.id가 같은 정상 기록끼리 비교
2) [901] 정렬 결과에서 가장 앞선 기록 하나만 유지

-> 중복으로 빠진 정상 기록은 excludedCount에 포함하지 않음.

[903] 순위 부여
1) [902]까지 완료한 최종 목록에 1부터 연속 순위 부여
2) 순위 오름차순으로 entries[] 구성


## ResponseDTO 구성

### 응답 필드
1) season = 02. meta.season.id
2) totalRecords = 10. records[]의 필터링 전 실제 개수
3) excludedCount = [900]에서 집계한 이상 기록 수
4) entries[] = [903]에서 순위를 부여한 최종 목록

-> totalRecords는 08. meta.totalRecords를 그대로 사용하지 않음.
-> entries[]는 순위 오름차순이며, 최종 기록이 없으면 빈 배열.

### entries[] 내부 (랭킹 항목)

1) rank = [903]에서 부여한 순위
2) playerName = 32. player.name
3) clearTimeSeconds = 44. run.durationSeconds
4) remainingHp = 45. run.finalHp
5) bossTurns = 75. bossFight.totalTurns
6) deckSize = 82. deck.cards[]의 실제 개수
