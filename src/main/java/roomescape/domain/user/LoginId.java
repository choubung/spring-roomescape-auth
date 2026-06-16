package roomescape.domain.user;

import roomescape.exception.InvalidDomainStateException;
import java.util.regex.Pattern;

public record LoginId (
        String loginId
) {
    private static final String ID_REGEX = "^[a-zA-Z][a-zA-Z0-9]{3,}$";

    public LoginId {
        if (loginId == null || loginId.isBlank()) {
            throw new InvalidDomainStateException("로그인 아이디는 비어있을 수 없습니다.");
        }

        if (!Pattern.matches(ID_REGEX, loginId)) {
            throw new InvalidDomainStateException("로그인 아이디는 4자 이상의 문자와 숫자로 이루어져야 하며, 문자로 시작해야합니다.");
        }
    }
}
