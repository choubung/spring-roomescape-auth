package roomescape.web.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import roomescape.dao.UserDao;
import roomescape.domain.user.LoginId;
import roomescape.domain.user.User;
import roomescape.exception.AuthenticationException;
import roomescape.global.auth.AuthInfo;

@RequiredArgsConstructor
public class LoginUserArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String LOGIN_INFO = "authInfo";
    private final UserDao userDao;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUser.class)
                && User.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        AuthInfo authInfo = (AuthInfo) request.getAttribute(LOGIN_INFO);

        LoginId loginId = new LoginId(authInfo.loginId());

        return userDao.findByLoginId(loginId)
                .orElseThrow(() -> new AuthenticationException("잘못된 로그인 정보입니다."));
    }
}
