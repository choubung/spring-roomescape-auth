package roomescape.web.dto.response;

import roomescape.domain.auth.Token;

public record LoginResponse(
        Token token
) {
}
