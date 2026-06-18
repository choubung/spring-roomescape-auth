package roomescape.web.common;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;
import roomescape.global.auth.AuthInfo;
import roomescape.global.auth.JwtProvider;

@RequiredArgsConstructor
public class LoginCheckInterceptor implements HandlerInterceptor {
    private final JwtProvider jwtProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            String jws = request.getHeader("Authorization").replace("Bearer ", "");
            AuthInfo authInfo = jwtProvider.extractAuthInfo(jws);

            request.setAttribute("authInfo", authInfo);

            return true;
        } catch (JwtException | NullPointerException | IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }
}
