package roomescape.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import roomescape.dao.UserDao;
import roomescape.domain.auth.Token;
import roomescape.domain.user.LoginId;
import roomescape.domain.user.Password;
import roomescape.domain.user.User;
import roomescape.global.auth.JwtProvider;
import roomescape.service.AuthService;
import roomescape.web.controller.AuthController;
import roomescape.web.dto.request.LoginRequest;
import roomescape.web.dto.request.SignUpRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    // 💡 [추가] 웹 설정(AuthenticationPrincipalConfig) 빌드 시
    // 누락되어 컨텍스트 로드를 깨뜨리던 의존성 빈들을 가짜 Mock 빈으로 주입합니다.
    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private UserDao userDao;

    @Test
    @DisplayName("회원가입 요청 시 정상적으로 201 CREATED 상태코드를 반환한다.")
    void signUp_Success() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("testId", "password123!", "홍길동", "USER");

        // when & then
        mockMvc.perform(post("/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(authService).signUp(any(User.class));
    }

    @Test
    @DisplayName("로그인 요청 시 정상적으로 토큰을 반환한다.")
    void login_Success() throws Exception {
        // given
        LoginRequest request = new LoginRequest("testId", "password123!");
        Token mockToken = new Token("generated-jwt-token-string");

        given(authService.login(any(LoginId.class), any(Password.class)))
                .willReturn(mockToken);

        // when & then
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token.value").value("generated-jwt-token-string"));
    }
}
