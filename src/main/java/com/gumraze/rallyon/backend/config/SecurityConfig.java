package com.gumraze.rallyon.backend.config;

import com.gumraze.rallyon.backend.auth.security.JwtAuthenticationFilter;
import com.gumraze.rallyon.backend.auth.token.JwtAccessTokenValidator;
import com.gumraze.rallyon.backend.common.logging.ApiLoggingFilter;
import com.gumraze.rallyon.backend.common.security.BotBlockFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtAccessTokenValidator jwtAccessTokenValidator
    ) {
        return new JwtAuthenticationFilter(jwtAccessTokenValidator);
    }

    @Bean
    public ApiLoggingFilter apiLoggingFilter() {
        return new ApiLoggingFilter();
    }

    @Bean
    public BotBlockFilter botBlockFilter() {
        return new BotBlockFilter();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter
    ) throws Exception {

        http
                // cors 설정
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // csrf 비활성화
                .csrf(csrf -> csrf.disable())

                // Stateless (세션 사용 안함)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 예외 처리
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(
                                (request, response, authException) ->
                                        writeErrorResponse(
                                                request,
                                                response,
                                                HttpStatus.UNAUTHORIZED,
                                                "/problems/unauthorized",
                                                "인증되지 않은 사용자입니다.",
                                                "인증되지 않은 사용자입니다."
                                        )
                        )
                        .accessDeniedHandler(
                                (request, response, accessDeniedException) ->
                                        writeErrorResponse(
                                                request,
                                                response,
                                                HttpStatus.FORBIDDEN,
                                                "/problems/forbidden",
                                                "접근 권한이 없습니다.",
                                                "접근 권한이 없습니다."
                                        )
                        )
                )

                // 권한 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/",
                                "/actuator/health"
                        ).permitAll()

                        .requestMatchers("/users/me").hasRole("USER")

                        // GET 요청 처리
                        .requestMatchers(
                                HttpMethod.GET,
                                "/regions/**",
                                "/free-games/share/**"
                        ).permitAll()
                        // POST 요청 처리
                        .requestMatchers(
                                HttpMethod.POST,
                                "/users/profile/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                // 봇 차단
                .addFilterBefore(
                        botBlockFilter(),
                        UsernamePasswordAuthenticationFilter.class
                )

                // JWT 필터 등록
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterAfter(
                        apiLoggingFilter(),     // 로깅 추가
                        JwtAuthenticationFilter.class
                )
        ;

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "https://api.drive-minton.com",
                "https://drive-minton.com",
                "http://localhost:3000",
                "http://localhost:3001",
                "http://localhost:3433",
                "https://localhost:3001",
                "https://localhost:3433",
                "https://rallyon.test"
        ));

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private void writeErrorResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpStatus status,
            String type,
            String title,
            String detail
    ) throws IOException {
        ProblemDetail problem = ProblemDetail.forStatus(status);
        problem.setType(URI.create(type));
        problem.setTitle(title);
        problem.setDetail(detail);
        if (request != null) {
            problem.setInstance(URI.create(request.getRequestURI()));
        }

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), problem);
    }


}
