# RallyOn Backend

Spring Boot 3.5 기반 RallyOn 백엔드 서비스입니다. 현재는 하나의 앱 안에서 다음 역할을 함께 제공합니다.

- `auth.rallyon.test`: Authorization Server, 프론트 로그인 UI, 브라우저 세션 어댑터
- `api.rallyon.test`: Resource Server API

## 주요 기능
- **인증/인가**: Spring Authorization Server 기반 Authorization Code + PKCE, OIDC discovery, Kakao/Google OAuth, 로컬 이메일 로그인, 로컬 개발용 DUMMY provider
- **세션 쿠키**: access token은 `.rallyon.test` 범위의 HttpOnly 쿠키, refresh token은 `auth.rallyon.test` 전용 HttpOnly 쿠키
- **사용자/프로필**: `/users/me` 조회, 프로필 등록·수정, 닉네임+태그 변경, 지역/성별/급수/생년월일 관리, 닉네임/태그 검색
- **지역**: Flyway로 시드된 시·군·구 목록 제공(`/regions/provinces`, `/regions/{provinceId}/districts`)
- **코트 매니저**: `/free-games` 생성, 상세 조회·수정, 라운드/매치 편성 조회 및 일괄 수정, 참가자 추가

## 기술 스택
- Java 21, Spring Boot 3.5, Spring MVC, Spring Security
- Spring Authorization Server, Spring Security OAuth2 Resource Server
- PostgreSQL, Flyway, Spring Data JPA
- Springdoc OpenAPI, H2(로컬/테스트), Gradle, Docker Compose

## 환경 변수
`src/main/resources/application.yml` 및 `application-*.yml`에서 다음 값을 읽습니다.

- 데이터베이스: `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`
- Authorization Server:
  - `APP_AUTH_ISSUER`
  - `APP_AUTH_FRONTEND_BASE_URL`
  - `APP_AUTH_BROWSER_REDIRECT_URI`
  - `APP_AUTH_ACCESS_TOKEN_COOKIE_DOMAIN`
  - `APP_API_HOST`
- OAuth provider:
  - `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`
  - `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_OAUTH_ENABLED`
  - `OAUTH_ALLOWED_PROVIDERS`, `OAUTH_DUMMY_ENABLED`
- 실행 프로필: `SPRING_PROFILES_ACTIVE=local|prod`

## 로컬 실행
1) DB만 필요할 때: `docker compose up db`  
2) 앱 실행(로컬 프로필): `./gradlew bootRun`  
   - 프로필을 지정하지 않으면 기본값으로 `local`이 적용됩니다. 필요하면 `SPRING_PROFILES_ACTIVE=local ./gradlew bootRun`처럼 명시할 수 있습니다.
   - H2 인메모리 DB로 포트 8080에서 HTTP 기동되며 `/` → `/swagger-ui/index.html` 로 리다이렉트됩니다.
3) 전체 빌드: `./gradlew build` (테스트 포함)  
4) 테스트만: `./gradlew test` (H2 사용, JUnit5)

## Docker 배포 실행
- 애플리케이션과 DB를 함께 올릴 때: `docker compose up`  
  - 앱 컨테이너는 `SPRING_PROFILES_ACTIVE=prod`로 기동하며 Swagger UI는 비활성화됩니다. 리소스 사용 제한은 Compose에 정의돼 있습니다.

## 데이터베이스 & 마이그레이션
- Flyway가 앱 기동 시 자동 실행(`baseline-on-migrate` 활성). 주요 스크립트: 초기 스키마(`V1__init_schema.sql`), 지역 데이터 시드(`V2__seed_region.sql`), 사용자/자유게임 관련 확장(V3~V10).
- 테스트는 H2 인메모리 DB를 사용하며 Flyway와 동일한 스키마로 검증합니다.

## Origin 빠른 참고
- Auth host: `https://auth.rallyon.test`
  - `/login` (프론트가 렌더링, 인증 처리는 백엔드 `/identity/**` 사용)
  - `/oauth2/**`
  - `/.well-known/**`
  - `/identity/session/start`
  - `/identity/session/callback`
  - `/identity/token/refresh`
  - `/identity/logout`
  - `POST /identity/password/register`
- API host: `https://api.rallyon.test`
  - `/users/**`
  - `/regions/**`
  - `/free-games/**`
  - `/actuator/health`
