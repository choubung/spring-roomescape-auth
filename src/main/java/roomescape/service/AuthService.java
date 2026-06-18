package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import roomescape.dao.UserDao;
import roomescape.domain.auth.Token;
import roomescape.domain.user.LoginId;
import roomescape.domain.user.Password;
import roomescape.domain.user.User;
import roomescape.exception.AuthenticationException;
import roomescape.exception.AuthorizationException;
import roomescape.exception.DuplicateException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.global.auth.JwtProvider;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDao userDao;
    private final JwtProvider jwtProvider;

    public User signUp(User user) {
        if (userDao.existsByLoginId(user.getLoginId())) {
            throw new DuplicateException("이미 존재하는 아이디입니다.");
        }

        Long savedId = userDao.create(user);

        return userDao.findById(savedId)
                .orElseThrow(() -> new ResourceNotFoundException("예약이 정상적으로 생성되지 않았습니다."));
    }

    public Token login(LoginId loginId, Password password) {
        User user = userDao.findByLoginId(loginId)
                .orElseThrow(() -> new ResourceNotFoundException("아아디 또는 비밀번호가 틀립니다."));

        if (!user.getPassword().equals(password)) {
            throw new AuthenticationException("아아디 또는 비밀번호가 틀립니다.");
        }

        return jwtProvider.generateToken(user);
    }

    public void validateAdmin(User user) {
        if (!user.isAdmin()) {
            throw new AuthorizationException("인증되지 않은 사용자입니다.");
        }
    }
}
