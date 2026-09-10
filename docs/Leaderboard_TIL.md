# Lv12 랭킹 시스템

## Ranking 시스템의 책임 흐름
1. 외부 랭킹 JSON
- 믿을 수 없는 표현 층의 데이터

1.1. RankingClient
- 외부 랭킹 API에 HTTP 요청
- JSON을 RankingSource 객체로 역직렬화

1.2. RankingSource
- 랭킹 판정과 응답 구성에 필요한 필드를 받는 DTO
- 외부 JSON 필드 이름과 구조를 그대로 표현.

2. RankingSourceMapper
- Ranking 시스템 내부 언어로 번역
- 값을 보정하지 않음

3. RankingRecord
- 번역된 채 판정을 기다리는 후보군

4. Gate
- 랭킹 참가 자격 판정
- NOT_ELIGIBLE / 검사 대상 구분

5. RecordInvariant
- 참가 대상 기록이 만족해야 하는 정상 조건
- 시간, HP, 덱, 카드, 보스전 일관성

6. RecordDiagnostic
- 어떤 정상 조건을 통과/위반했는지 판정
- VALID / INVALID_* 등

7. RankingPolicy
- 판정된 기록을 전체 랭킹에서 어떻게 취급할지 결정
- 이상 기록 제외 및 집계
- 정상 기록 정렬
- 플레이어 중복 제거
- 순위 부여
