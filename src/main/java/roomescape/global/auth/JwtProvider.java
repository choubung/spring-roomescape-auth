package roomescape.global.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import roomescape.domain.auth.Token;
import roomescape.domain.user.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtProvider {
    private static final int EXPIRATION_MINUTE = 30;
    private final SecretKey KEY;

    public JwtProvider(@Value("${JWT_SECRET_KEY}") String jwtSecret) {
        this.KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Token generateToken(User user) {
        Date expiration = Date.from(Instant.now().plus(Duration.ofMinutes(EXPIRATION_MINUTE)));

        String jws = Jwts.builder()
                .subject(user.getLoginId().value())
                .claim("role", user.getRole())
                .expiration(expiration)
                .signWith(KEY)
                .compact();

        return new Token(jws);
    }

    public AuthInfo extractAuthInfo(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String loginId = claims.getSubject();
        String role = claims.get("role", String.class);

        return new AuthInfo(loginId, role);
    }
}
