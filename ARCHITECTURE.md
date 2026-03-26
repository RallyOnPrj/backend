# RallyOn Architecture

이 문서는 RallyOn 워크스페이스의 아키텍처 기준선입니다.
워크스페이스 루트는 Git 저장소가 아니므로, canonical 문서는 `backend/` 저장소에 둡니다.
대상 범위는 `backend`, `frontend`, `infra` 전체입니다.

## 1. 원칙

- 유비쿼터스 언어를 먼저 고정한다.
- 복잡한 도메인은 DDD 기반 헥사고날 아키텍처를 사용한다.
- 단순한 도메인은 레이어드 아키텍처를 유지한다.
- 아직 1.0 이전이므로 문서와 새 코드는 compatibility alias 없이 canonical 이름만 사용한다.

## 2. Workspace 역할

### `backend`

- RallyOn의 모듈러 모놀리식 애플리케이션이다.
- 도메인 규칙, 인증/인가 규칙, 데이터 일관성의 최종 진실 원천이다.
- 워크스페이스 전체 아키텍처의 source of truth를 맡는다.

### `frontend`

- 사용자 화면과 브라우저 상호작용을 담당한다.
- 로그인, 회원가입, 프로필 작성, 서비스 UI를 제공한다.
- 도메인 규칙의 최종 진실 원천이 아니다.

### `infra`

- 로컬 및 배포 실행 환경을 담당한다.
- 호스트, 인증서, Compose, reverse proxy 같은 런타임 경계를 설명한다.
- 도메인 규칙이나 모듈 책임을 소유하지 않는다.

## 3. 논리 모듈 맵

| 모듈 | 책임 | 복잡도 | 아키텍처 |
| --- | --- | --- | --- |
| `identity` | 계정, 로컬 자격증명, OAuth Link, provider 연동, 인증 결과 생성 | 높음 | 헥사고날 |
| `authorization` | Browser Auth Session, PKCE/state, authorization code, token, JWK/JWKS, OIDC metadata, client 정책 | 높음 | 헥사고날 |
| `user` | 사용자 요약, 프로필 생성/조회/수정, 상태 전환, Profile Defaults | 높음 | 헥사고날 |
| `courtManager` | 자유게임 운영, 참가자/라운드/매치 규칙 | 높음 | 헥사고날 |
| `region` | 지역 참조 데이터 조회 | 낮음 | 레이어드 |
| `place-search` | 외부 장소 검색 연동 | 낮음 | 레이어드 |
| `common`, `config`, `security` | 횡단 관심사와 기술 지원 | 횡단 | 공통 인프라 |
| `frontend/auth` | 로그인, 회원가입, 프로필 작성 UI | 중간 | 기능 단위 모듈화 |
| `infra` | 실행 환경, 호스트, 인증서, Compose | 낮음 | 문서/구성 |

규칙:

- `authorization`는 별도 배포 서비스가 아니라 `backend` 내부의 논리 모듈이다.
- 문서, 패키지, 타입, 테스트, API 설명은 이 모듈 이름을 따른다.
- 구현 기술명은 설명에만 사용하고, 모듈명으로 쓰지 않는다.

## 4. 유비쿼터스 언어

### 공통

- `회원가입`: 인증 계정을 만드는 행위
- `로그인`: 이미 존재하는 계정으로 인증을 완료하는 행위
- `온보딩`: 회원가입 또는 첫 로그인 이후 프로필 작성까지 완료하는 흐름

### Identity

- `Identity Account`: 인증 주체 계정
- `Local Credential`: 이메일/비밀번호 자격증명
- `OAuth Link`: 외부 OAuth provider와 연결된 자격증명
- `Auth Provider`: KAKAO, GOOGLE, APPLE, DUMMY 같은 로그인 제공자
- `Authenticated Identity`: 인증 완료 결과로 얻는 주체 표현
- `identity_accounts`: 인증 주체의 canonical 저장 모델

### Authorization

- `Authorization`: OAuth 2.1/OIDC 인가 흐름
- `Browser Auth Session`: 브라우저 로그인/회원가입/OAuth 콜백을 이어주는 짧은 수명 세션
- `Browser Client`: Authorization Code + PKCE를 사용하는 1st-party 웹 클라이언트
- `Authorization Code`: 브라우저 세션 완료 후 token pair로 교환되는 코드
- `Token Pair`: access token과 refresh token

### User / Profile

- `User Module`: RallyOn 프로필과 온보딩 상태를 소유하는 모듈
- `User Status`: `PENDING` 또는 `ACTIVE`
- `Profile`: RallyOn 서비스 사용을 위한 사용자 정보 집합
- `Profile Defaults`: 소셜 로그인 등에서 가져온 추천 초기값
- `Nickname`: 공개 이름
- `Tag`: 닉네임 중복을 구분하는 공개 식별 보조값
- `PENDING`: 프로필이 아직 없는 계정 상태
- `ACTIVE`: 프로필이 생성된 계정 상태

### Region

- `Province`: 시/도 단위 지역
- `District`: 시/군/구 단위 지역
- `Region Reference Data`: 자주 바뀌지 않는 참조 데이터

### Court Manager

- `Free Game`: RallyOn에서 운영하는 자유게임
- `Organizer`: 게임 생성자 및 운영 책임자
- `Participant`: 게임 참가자
- `Round`: 한 차수의 경기 편성
- `Match`: 특정 코트에서 열리는 경기
- `Organizer`, `Participant`, `Manager`는 모두 `accountId`로 식별한다

### 용어 규칙

- `defaults`를 프로필 추천 초기값의 유일한 용어로 사용한다.
- `identity`는 인증 주체와 자격증명 도메인에만 사용한다.
- `authorization`는 OAuth 2.1/OIDC 프로토콜 흐름 도메인에만 사용한다.
- `profile`은 RallyOn 서비스 프로필을 뜻할 때만 사용한다.
- `session`은 Browser Auth Session을 뜻할 때만 사용한다.
- 구현 기술명은 모듈명보다 우선하지 않는다.

## 5. 아키텍처 규칙

### 5.1 복잡한 도메인: 헥사고날

대상:

- `identity`
- `authorization`
- `user`
- `courtManager`

권장 패키지 구조:

```text
module/
├─ adapter/
│  ├─ in/
│  └─ out/
├─ application/
│  ├─ port/
│  │  ├─ in/
│  │  └─ out/
│  └─ service/
└─ domain/
```

규칙:

- `adapter/in`은 HTTP, scheduler, 메시지 같은 입력 어댑터만 둔다.
- `adapter/out`은 persistence, 외부 API, 보안, 파일 시스템 같은 출력 어댑터만 둔다.
- `application/port/in`은 use case 계약을 둔다.
- `application/port/out`은 외부 의존성 계약을 둔다.
- `application/service`는 유스케이스 조합과 트랜잭션 경계를 담당한다.
- `domain`은 엔티티, 값 객체, 정책, 팩토리, 도메인 서비스만 둔다.
- DTO, command, query, result는 기본적으로 `record`를 우선한다.
- `@ConfigurationProperties`는 작고 immutable한 경우에만 `record`를 사용하고, 중첩 설정과 기본값이 많은 경우에는 `class`를 유지한다.

금지:

- application service가 request/response DTO를 직접 import하는 것
- application service가 repository를 직접 참조하는 것
- domain이 Spring, Servlet, repository 타입에 의존하는 것
- 하나의 모듈 안에서 의미 없는 별도 `service` 계층을 병행하는 것

예외:

- Spring Security나 OAuth 2.1 프레임워크 경계처럼 결합이 불가피한 부분은 `adapter` 또는 `config`에 둔다.
- 이 경우에도 request/response, servlet 타입은 가능한 한 adapter에서 소진하고 안쪽에는 command/result만 전달한다.

### 5.2 단순한 도메인: 레이어드

대상:

- `region`
- `place-search`

권장 구조:

```text
module/
├─ web
├─ service
├─ repository
└─ dto
```

규칙:

- 포트/어댑터를 억지로 만들지 않는다.
- 단순 조회, 참조 데이터 제공, 외부 API pass-through 같은 얕은 규칙의 기능에만 사용한다.
- 그래도 controller가 repository를 직접 호출하지는 않는다.

금지:

- 단순 모듈이 복잡 도메인의 구현 세부를 직접 참조하는 것
- 레이어드 모듈 안에 일부만 헥사고날 패키지를 섞는 것

## 6. 모듈 의존성 규칙

- `authorization -> identity` 의존만 허용한다.
  - 인가 흐름은 인증된 주체 표현을 필요로 한다.
- `identity -> authorization` 의존은 금지한다.
  - 계정/자격증명 도메인은 인가 프로토콜 세부 구현을 몰라야 한다.
- `user -> identity`는 식별 계약 또는 읽기 계약만 참조한다.
- `courtManager -> user`는 참가자/주최자 식별 차원에서만 참조한다.
- `region`은 참조 데이터 모듈이므로 다른 도메인이 읽을 수 있다.
- `common`, `config`, `security`는 횡단 관심사만 두고 비즈니스 규칙을 소유하지 않는다.

## 7. 공개 경계

런타임 공개 경계는 host 기준으로 유지한다.

- `auth.rallyon.test`
  - auth UI
  - `/identity/**`
  - `/oauth2/**`
  - `/.well-known/**`
- `api.rallyon.test`
  - resource APIs

현재 canonical auth/profile 경계:

- `GET /identity/sessions/current`
- `POST /identity/sessions`
- `POST /identity/sessions/local`
- `POST /identity/registrations/local`
- `POST /identity/accounts/local`
- `GET /identity/oauth/{provider}`
- `GET|POST /identity/oauth/{provider}/callback`
- `POST /identity/tokens/refresh`
- `DELETE /identity/sessions/current`
- `GET /users/me/profile/defaults`
- `POST /users/me/profile`
- `GET /users/me/profile`
- `PATCH /users/me/profile`

문서와 새 코드는 canonical 이름만 사용한다.

## 8. DTO / record / Lombok / Factory 규칙

### DTO / Command / Query / Result

- 불변 전달 객체는 기본적으로 `record`를 우선 사용한다.
- 대상:
  - request DTO
  - response DTO
  - command
  - query
  - result
  - summary/projection
- 예외:
  - JPA entity
  - 프레임워크 제약으로 no-args 생성자가 꼭 필요한 타입

### Lombok

기본 허용:

- `@RequiredArgsConstructor`
- `@Slf4j`
- 정말 필요한 최소한의 `@Getter`

지양:

- `@Data`
- 무분별한 `@Setter`
- request/response DTO에 대한 습관적 `@Builder`
- 엔티티 생성 편의를 위한 `@Builder`

### Builder / Factory

- 기본 원칙은 `Builder 최소화`, `의미 있는 팩토리 메서드 우선`이다.
- 엔티티와 값 객체는 `create`, `issue`, `link`, `start`, `complete` 같은 정적 팩토리 메서드를 우선 사용한다.
- 테스트 데이터 생성이나 응답 조립처럼 꼭 필요한 경우에만 `Builder`를 허용한다.
- 경계 객체와 도메인 생성에 습관적으로 `builder()`를 열지 않는다.

## 9. 구현 방향

- 새 인증 주체 관련 코드는 `identity`에 둔다.
- 새 인가 흐름 관련 코드는 `authorization`에 둔다.
- 프로필 추천 초기값은 `user` 모듈이 소유한다.
- 단순 도메인은 과설계하지 않고 레이어드로 유지한다.
- 리팩터링은 “문서와 같은 이름, 같은 경계”를 강제한다.

이 문서는 이상적인 미래상을 그리는 문서가 아니라, RallyOn이 지금부터 같은 용어와 같은 경계로 정리되기 위한 기준선이다.
