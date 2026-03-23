package com.gumraze.rallyon.backend.identity.adapter.in.web;

import com.gumraze.rallyon.backend.identity.adapter.out.oauth.OAuthAllowedProvidersProperties;
import com.gumraze.rallyon.backend.identity.authorizationserver.domain.BrowserAuthorizationRequestContext;
import com.gumraze.rallyon.backend.identity.authorizationserver.support.BrowserAuthorizationRequestContextRepository;
import com.gumraze.rallyon.backend.identity.domain.authentication.AuthProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class IdentityLoginPageController {

    private final OAuthAllowedProvidersProperties allowedProviders;
    private final BrowserAuthorizationRequestContextRepository contextRepository;

    public IdentityLoginPageController(
            OAuthAllowedProvidersProperties allowedProviders,
            BrowserAuthorizationRequestContextRepository contextRepository
    ) {
        this.allowedProviders = allowedProviders;
        this.contextRepository = contextRepository;
    }

    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> loginPage(
            HttpServletRequest request,
            @RequestParam(required = false) String error
    ) {
        Optional<BrowserAuthorizationRequestContext> context = Optional.ofNullable(request.getSession(false))
                .flatMap(contextRepository::load);
        String returnTo = context.map(BrowserAuthorizationRequestContext::returnTo).orElse("/profile");

        StringBuilder html = new StringBuilder()
                .append("<!doctype html><html lang=\"ko\"><head><meta charset=\"utf-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<title>RallyOn Login</title>")
                .append("<style>")
                .append("body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;margin:0;background:#f7f7fb;color:#111;}")
                .append(".wrap{max-width:560px;margin:96px auto;padding:0 24px;}")
                .append("h1{font-size:48px;margin:0 0 24px;line-height:1.1;}")
                .append("p{margin:0 0 16px;color:#555;}")
                .append("form,input,button,a{font:inherit;}")
                .append("input{width:100%;padding:14px 16px;margin-bottom:12px;border:1px solid #d7d7e0;border-radius:12px;box-sizing:border-box;}")
                .append("button,.link{display:block;width:100%;padding:14px 16px;border-radius:12px;border:none;text-align:center;text-decoration:none;box-sizing:border-box;margin-bottom:12px;}")
                .append("button{background:#111;color:#fff;cursor:pointer;}")
                .append(".divider{margin:28px 0 20px;border-top:1px solid #e6e6ee;}")
                .append(".link{background:#fff;border:1px solid #d7d7e0;color:#111;}")
                .append(".dummy{font-size:15px;padding:12px 16px;}")
                .append(".error{background:#fff1f1;color:#b42318;padding:12px 14px;border-radius:12px;margin-bottom:16px;}")
                .append("</style></head><body><main class=\"wrap\">")
                .append("<h1>RallyOn 로그인</h1>");

        if (error != null && !error.isBlank()) {
            html.append("<div class=\"error\">로그인에 실패했습니다. 다시 시도해주세요. (")
                    .append(escape(error))
                    .append(")</div>");
        }

        html.append("<p>인증을 완료하면 원래 보시던 화면으로 돌아갑니다.</p>")
                .append("<form method=\"post\" action=\"/identity/local/login\">")
                .append("<input type=\"email\" name=\"email\" placeholder=\"이메일\" autocomplete=\"username\" required>")
                .append("<input type=\"password\" name=\"password\" placeholder=\"비밀번호\" autocomplete=\"current-password\" required>")
                .append("<button type=\"submit\">이메일 로그인</button>")
                .append("</form>")
                .append("<div class=\"divider\"></div>");

        if (allowedProviders.getAllowedProviders().contains(AuthProvider.KAKAO)) {
            html.append("<a class=\"link\" href=\"/identity/social/start/KAKAO\">카카오 로그인</a>");
        }
        if (allowedProviders.getAllowedProviders().contains(AuthProvider.GOOGLE)) {
            html.append("<a class=\"link\" href=\"/identity/social/start/GOOGLE\">구글 로그인</a>");
        }
        if (allowedProviders.getAllowedProviders().contains(AuthProvider.DUMMY)) {
            html.append("<div class=\"divider\"></div><p>로컬 테스트 로그인</p>")
                    .append(dummyLink("manager-local", "DUMMY 로그인 (manager-local)", returnTo))
                    .append(dummyLink("player-local", "DUMMY 로그인 (player-local)", returnTo))
                    .append(dummyLink("fresh-20ab6990", "DUMMY 로그인 (fresh-20ab6990)", returnTo));
        }

        html.append("</main></body></html>");
        return ResponseEntity.ok(html.toString());
    }

    private String dummyLink(String code, String label, String returnTo) {
        return "<a class=\"link dummy\" href=\"/identity/session/start?provider=DUMMY&dummyCode="
                + escape(code)
                + "&returnTo="
                + escape(returnTo)
                + "\">"
                + escape(label)
                + "</a>";
    }

    private String escape(String value) {
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
