# Backend Agent Guide

This file defines the working rules for agents and contributors operating inside the RallyOn backend repository.

## Project Context

- Stack: Java 21, Spring Boot 3.5, Spring Web, Spring Security, OAuth2 Client, JPA, Flyway, PostgreSQL, JWT, springdoc OpenAPI
- Build tool: Gradle
- Main package root: `com.gumraze.rallyon.backend`
- Main verification command: `./gradlew test`

## Architecture Expectations

The backend follows a package-by-domain structure with layered internals.

- `auth`, `user`, `region`, `courtManager`: domain-oriented packages
- `controller`: web adapters and HTTP handling
- `service`: use-case orchestration and business logic
- `repository`: persistence access only
- `entity`: JPA entities and persistence models
- `dto`: request and response models
- `api`: API contracts and interface-level definitions
- `config`, `common`: cross-cutting configuration, exceptions, logging, security helpers
- `web`: simple web-specific endpoints or redirects outside the main domain packages

Keep controllers thin, keep business decisions in services, and keep repositories focused on data access.
Do not move domain logic into controllers, filters, or configuration classes.

## Engineering Principles

### TDD

TDD is the default approach for business logic changes, bug fixes, and new behavior.

- Start with a failing test when the behavior is clear enough to specify.
- Add or update tests before changing service logic whenever practical.
- Prefer focused tests around use cases, validation rules, security behavior, and persistence boundaries.
- Do not add shallow tests that only assert framework wiring unless the wiring itself is the change.

#### Kent Beck Style TDD Policy

Use Kent Beck style TDD as the default operating loop for backend behavior changes.

- Always work in the `red -> green -> refactor` cycle.
- Start with the smallest failing test that expresses one concrete behavior.
- Make the test fail for the right reason before changing production code.
- In the green step, do the minimum work needed to pass the current test.
- Once green, refactor code and tests while keeping the whole suite green.
- If a new case appears while working, add it to a test list and return to the current cycle first.
- Prefer simple TDD moves: obvious implementation, fake it, then triangulate when needed.
- Do not mix new behavior with refactoring while tests are red.
- If the design feels wrong, get back to green first, then improve the design in small safe steps.
- Keep the feedback loop short; small changes and frequent test runs are preferred over large jumps.

#### TDD Execution Expectations

- For each behavior change, capture at least one failing test before the production change.
- Name tests by behavior, not implementation detail.
- Keep one test focused on one reason to fail.
- When a bug is fixed, first reproduce it with a failing regression test.
- Refactoring is only complete when the relevant test set and the full backend suite stay green.

### DRY

- Remove real duplication, not incidental similarity.
- Extract shared logic only after the duplication is stable and proven.
- Prefer small local refactors over premature shared abstractions.

### KISS

- Prefer straightforward Spring patterns over clever abstractions.
- Favor explicit service methods, simple DTOs, and readable transaction boundaries.
- Avoid framework-heavy indirection unless it clearly reduces complexity.

### YAGNI

- Do not introduce new layers, base classes, generic utilities, or extension points without an immediate use case.
- Do not design for speculative future modules.
- Keep configuration surface area as small as possible.

## Coding Rules

- Prefer constructor injection. Do not introduce field injection.
- Keep validation close to request boundaries and use explicit domain validation in services where needed.
- Keep transactions intentional. Add them where business consistency requires them, not by default everywhere.
- Prefer descriptive method names over comments that explain obvious behavior.
- Preserve package boundaries. If a change spans multiple domains, make the use-case boundary explicit.
- Keep security-sensitive code explicit and easy to audit.

## Testing Rules

- Run `./gradlew test` for meaningful backend changes.
- Add regression tests for bugs before or alongside the fix.
- When changing security, auth, or token behavior, add tests that cover both allowed and denied flows.
- When changing persistence behavior, verify repository and service interactions with realistic test coverage.

## Git Workflow

Use this flow for backend work:

1. Create an issue.
2. Create a branch from that issue.
3. Commit with the agreed convention.
4. Open a PR.
5. Merge with squash merge.

### Branch Naming

- Format: `type/{issue-number}-{slug}`
- Examples:
  - `feat/123-login-page`
  - `fix/87-cors`
  - `refactor/201-auth-service-cleanup`

Allowed `type` values:

- `feat`
- `fix`
- `refactor`
- `docs`
- `chore`
- `test`
- `style`

### Commit Convention

Use Conventional Commits with an English type and a Korean subject line.

- Format: `type: Korean summary`
- Examples:
  - `feat: 카카오 로그인 리다이렉트 수정`
  - `fix: 사용자 프로필 조회 null 처리 보완`
  - `refactor: JWT 검증 흐름 분리`

PR titles must follow the same format because squash merge uses the PR title as the final commit title.

## Change Scope Discipline

- Keep each PR focused on one issue.
- Do not mix unrelated refactors with functional changes.
- If a repository-wide policy or documentation change affects developer workflow, it is usually better tracked as a separate backend issue.

## Practical Guidance For Agents

- Read existing domain structure before introducing new packages.
- Match the current architecture instead of inventing a new one mid-change.
- Prefer updating existing tests and flows over adding parallel patterns.
- If a requested change conflicts with TDD, DRY, KISS, or YAGNI, simplify the approach before implementing it.
