package com.gumraze.rallyon.backend.authorization.application.port.in;

import com.gumraze.rallyon.backend.authorization.domain.BrowserAuthSession;
import com.gumraze.rallyon.backend.authorization.domain.TokenResponse;
import com.gumraze.rallyon.backend.identity.domain.AuthProvider;
import com.gumraze.rallyon.backend.identity.domain.AuthenticatedAccount;

import java.util.List;

/**
 * 브라우저 기반 인증 흐름을 조정하는 애플리케이션 포트다.
 * <p>
 * 로그인/회원가입 화면 진입, OAuth 시작과 콜백 처리,
 * 브라우저 세션 콜백, 토큰 갱신과 로그아웃까지
 * 웹 인증 화면에 필요한 계약을 정의한다.
 */
public interface BrowserAuthorizationUseCase {

    /**
     * 현재 브라우저 인증 세션을 화면에 노출할 수 있는 형태로 변환한다.
     * <p>
     * 허용된 소셜 로그인 수단과
     * DUMMY 로그인 옵션도 함께 계산한다.
     */
    CurrentSessionView getCurrentSession(BrowserAuthSession currentSession);

    /**
     * 브라우저 인증 세션을 시작하거나 갱신한다.
     * <p>
     * 화면 종류, 복귀 경로, 요청한 로그인 제공자에 따라
     * 다음 이동 경로를 결정한다.
     */
    SessionStartResult startSession(StartSessionCommand command);

    /**
     * 로컬 이메일/비밀번호 로그인 흐름을 처리한다.
     * <p>
     * 성공 시 인가 코드 교환 단계로 이동할
     * 리다이렉트 URL을 반환한다.
     */
    AuthorizationStepResult loginWithLocalSession(LocalLoginCommand command);

    /**
     * 로컬 회원가입을 처리하고,
     * 성공 시 즉시 로그인 흐름으로 이어지도록
     * 다음 이동 경로를 반환한다.
     */
    AuthorizationStepResult registerWithLocalSession(LocalRegistrationCommand command);

    /**
     * 지정된 로그인 제공자의 인증을 시작한다.
     * <p>
     * 일반 OAuth 제공자는 외부 인가 URL로,
     * DUMMY 제공자는 내부 테스트 인증 흐름으로 이동한다.
     * <p>
     * 호출 시점에는 유효한 브라우저 인증 세션이 준비되어 있어야 한다.
     */
    AuthorizationStepResult startOAuth(StartOAuthCommand command);

    /**
     * 소셜 로그인 제공자 콜백을 처리한다.
     * <p>
     * 콜백 검증과 사용자 식별을 마친 뒤
     * 다음 인가 단계 또는 오류 화면으로 이동할 정보를 반환한다.
     */
    AuthorizationStepResult handleOAuthCallback(OAuthCallbackCommand command);

    /**
     * 브라우저 인가 코드 세션 콜백을 처리한다.
     * <p>
     * 완료 시 토큰 발급 결과를 포함하고,
     * 미완료 시 추가 리다이렉트 정보만 반환할 수 있다.
     */
    SessionCallbackResult handleSessionCallback(SessionCallbackCommand command);

    /**
     * refresh token으로 브라우저 세션 토큰을 재발급한다.
     * <p>
     * 유효하지 않은 토큰이 전달되면 구현은 인증 예외를 발생시킬 수 있다.
     */
    TokenRefreshResult refresh(String refreshToken);

    /**
     * refresh token을 기준으로 브라우저 세션을 종료한다.
     * <p>
     * 브라우저 쿠키 만료 자체는 이 포트 바깥의 웹 어댑터가 담당한다.
     */
    void logout(String refreshToken);

    /**
     * 인증 화면 진입 시 필요한 입력값을 담는다.
     */
    record StartSessionCommand(
            AuthProvider provider,
            String dummyCode,
            String screen,
            String returnTo,
            AuthenticatedAccount currentIdentity
    ) {
    }

    /**
     * 인증 세션 시작 후 저장할 세션 정보와 다음 이동 경로를 함께 반환한다.
     */
    record SessionStartResult(
            BrowserAuthSession authSession,
            String nextUrl,
            AuthenticatedAccount authenticatedAccount
    ) {
    }

    /**
     * 로그인/회원가입 화면을 렌더하기 위한 현재 세션 뷰다.
     */
    record CurrentSessionView(
            boolean hasSession,
            String returnTo,
            String screen,
            List<AuthProvider> allowedProviders,
            List<DummyLoginOption> dummyOptions
    ) {
    }

    /**
     * 로컬 개발 환경에서 빠른 로그인에 사용하는 DUMMY 로그인 옵션이다.
     */
    record DummyLoginOption(String label, String startUrl) {
    }

    /**
     * 로컬 이메일 로그인 요청의 입력값이다.
     * <p>
     * 이미 생성된 브라우저 인증 세션을 바탕으로
     * 로그인 후 다음 인가 단계로 이어진다.
     */
    record LocalLoginCommand(
            String email,
            String password,
            String returnTo,
            BrowserAuthSession authSession
    ) {
    }

    /**
     * 로컬 회원가입 요청의 입력값이다.
     * <p>
     * 회원가입 성공 시 동일한 브라우저 인증 세션으로
     * 로그인 흐름을 계속 진행한다.
     */
    record LocalRegistrationCommand(
            String email,
            String password,
            String passwordConfirm,
            String returnTo,
            BrowserAuthSession authSession
    ) {
    }

    /**
     * OAuth 인증 시작에 필요한 입력값이다.
     * <p>
     * DUMMY provider를 사용할 때는 dummyCode가 함께 전달될 수 있다.
     */
    record StartOAuthCommand(
            AuthProvider provider,
            String dummyCode,
            BrowserAuthSession authSession,
            String callbackPath
    ) {
    }

    /**
     * OAuth 제공자 콜백 처리에 필요한 입력값이다.
     * <p>
     * provider별 인증 결과를
     * 현재 브라우저 인증 세션과 연결할 때 사용한다.
     */
    record OAuthCallbackCommand(
            AuthProvider provider,
            String code,
            String state,
            String error,
            BrowserAuthSession authSession,
            String callbackPath
    ) {
    }

    /**
     * OAuth 시작, 콜백 처리,
     * 로컬 로그인/회원가입 이후의 다음 이동 위치를 표현한다.
     * <p>
     * 인증된 계정이 이미 확정된 경우 함께 반환한다.
     */
    record AuthorizationStepResult(
            String redirectLocation,
            AuthenticatedAccount authenticatedAccount
    ) {
    }

    /**
     * 브라우저 인가 코드 세션 콜백 처리에 필요한 입력값을 담는다.
     */
    record SessionCallbackCommand(
            String code,
            String state,
            String error,
            BrowserAuthSession authSession,
            AuthenticatedAccount currentIdentity
    ) {
    }

    /**
     * 브라우저 세션 콜백 처리 결과다.
     * <p>
     * 완료 여부에 따라 최종 토큰 발급 결과를 포함하거나
     * 추가 리다이렉트만 반환할 수 있다.
     */
    record SessionCallbackResult(
            boolean completed,
            String redirectLocation,
            TokenResponse tokenResponse,
            boolean clearSession
    ) {
        public static SessionCallbackResult redirectOnly(
                String redirectLocation,
                boolean clearSession
        ) {
            return new SessionCallbackResult(false, redirectLocation, null, clearSession);
        }

        public static SessionCallbackResult completed(
                String redirectLocation,
                TokenResponse tokenResponse,
                boolean clearSession
        ) {
            return new SessionCallbackResult(true, redirectLocation, tokenResponse, clearSession);
        }
    }

    /**
     * 토큰 재발급 결과를 담는다.
     */
    record TokenRefreshResult(
            String accessToken,
            String refreshToken
    ) {
    }
}
