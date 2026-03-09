# Contributing

## 기본 흐름

1. Issue를 생성합니다.
2. `type/{issue-number}-{slug}` 형식의 브랜치를 만듭니다.
3. `type: 한글 제목` 형식으로 커밋합니다.
4. PR 제목도 같은 형식으로 작성합니다.
5. Squash merge로 병합합니다.

## 브랜치 규칙

- 형식: `type/{issue-number}-{slug}`
- 예시: `feat/123-login-page`, `fix/87-cors`

허용 타입:

- `feat`
- `fix`
- `refactor`
- `docs`
- `chore`
- `test`
- `style`

## 커밋 / PR 제목 규칙

- 형식: `type: 한글 제목`
- 예시: `feat: 카카오 로그인 리다이렉트 수정`

자동 검증은 형식만 확인합니다.
한글 제목 사용은 팀 규칙으로 운영합니다.

## 검증 명령

- `./gradlew test`

## 참고

- GitHub issue/PR 템플릿과 `policy.yml` 워크플로가 같은 규칙을 검증합니다.
- 실제 GitHub 저장소의 squash merge 허용/브랜치 보호 설정은 저장소 관리자 권한으로 별도 맞춰야 합니다.
