package com.gumraze.rallyon.backend.api.auth;

import com.gumraze.rallyon.backend.api.common.ApiBadRequestResponse;
import com.gumraze.rallyon.backend.api.common.ApiServerErrorResponse;
import com.gumraze.rallyon.backend.auth.dto.OAuthLoginRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@Tag(name = "Auth", description = "OAuth 로그인/토큰 관련 API")
public interface AuthApi {

    @Operation(
            summary = "OAuth 로그인",
            description = "외부 OAuth 공급자로 로그인 후 access_token, refresh_token 쿠키를 발급합니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OAuthLoginRequestDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "provider": "DUMMY",
                                              "authorizationCode": "auth-code",
                                              "redirectUri": "https://test.com"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiBadRequestResponse
    @ApiServerErrorResponse
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "로그인 성공 (No Content, Set-Cookie: access_token, refresh_token)",
                    content = @Content
            )
    })
    ResponseEntity<Void> login(
            OAuthLoginRequestDto request
    );

    @Operation(
            summary = "Access 토큰 리프레시",
            description = "refresh_token 쿠키로 새로운 access_token, refresh_token 쿠키를 재발급합니다."
    )
    @ApiBadRequestResponse
    @ApiServerErrorResponse
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "리프레시 성공 (Set-Cookie: access_token, refresh_token)",
                    content = @Content
            )
    })
    ResponseEntity<Void> refresh(
            @Parameter(
                    name = "refresh_token",
                    description = "Refresh Token cookie",
                    in = ParameterIn.COOKIE,
                    required = false
            )
            String refreshToken
    );

    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 무효화하고 access_token, refresh_token 쿠키를 만료 처리합니다."
    )
    @ApiServerErrorResponse
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "로그아웃 성공 (No Content)",
                    content = @Content
            )
    })
    ResponseEntity<Void> logout(
            @Parameter(
                    name = "refresh_token",
                    description = "Refresh Token cookie",
                    in = ParameterIn.COOKIE,
                    required = false
            )
            String refreshToken
    );
}
