package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.dao.UserDao;
import roomescape.domain.auth.Token;
import roomescape.domain.user.Password;
import roomescape.domain.user.LoginId;
import roomescape.domain.user.User;
import roomescape.domain.user.UserName;
import roomescape.exception.AuthenticationException;
import roomescape.exception.DuplicateException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.global.auth.JwtProvider;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private JwtProvider jwtProvider;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private LoginId loginId;
    private Password password;

    @BeforeEach
    void setUp() {
        loginId = new LoginId("testId");
        password = new Password("password123!");
        sampleUser = User.createUser(loginId, password, UserName.from("홍길동"));
    }

    @Test
    @DisplayName("회원가입 시 중복된 아이디가 없으면 정상적으로 가입되고 유저 정보를 반환한다.")
    void signUp_Success() {
        // given
        given(userDao.existsByLoginId(loginId)).willReturn(false);
        given(userDao.create(any(User.class))).willReturn(1L);
        given(userDao.findById(1L)).willReturn(Optional.of(sampleUser));

        // when
        User result = authService.signUp(sampleUser);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getLoginId()).isEqualTo(loginId);
    }

    @Test
    @DisplayName("회원가입 시 이미 존재하는 아이디라면 DuplicateException이 발생한다.")
    void signUp_ThrowsDuplicateException() {
        // given
        given(userDao.existsByLoginId(sampleUser.getLoginId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signUp(sampleUser))
                .isInstanceOf(DuplicateException.class)
                .hasMessageContaining("이미 존재하는 아이디입니다.");
    }

    @Test
    @DisplayName("로그인 시 아이디와 비밀번호가 일치하면 토큰을 발급한다.")
    void login_Success() {
        // given
        Token mockToken = new Token("jwt-token");
        given(userDao.findByLoginId(loginId)).willReturn(Optional.of(sampleUser));
        given(jwtProvider.generateToken(sampleUser)).willReturn(mockToken);

        // when
        Token result = authService.login(loginId, password);

        // then
        assertThat(result.value()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("로그인 시 존재하지 않는 아이디인 경우 ResourceNotFoundException이 발생한다.")
    void login_ThrowsResourceNotFoundException() {
        // given
        given(userDao.findByLoginId(loginId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(loginId, password))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("아아디 또는 비밀번호가 틀립니다.");
    }

    @Test
    @DisplayName("로그인 시 비밀번호가 일치하지 않으면 IllegalArgumentException이 발생한다.")
    void login_ThrowsIllegalArgumentException() {
        // given
        Password wrongPassword = new Password("wrong_password");
        given(userDao.findByLoginId(loginId)).willReturn(Optional.of(sampleUser));

        // when & then
        assertThatThrownBy(() -> authService.login(loginId, wrongPassword))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("아아디 또는 비밀번호가 틀립니다.");
    }
}
