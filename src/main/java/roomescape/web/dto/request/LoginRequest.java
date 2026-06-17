package roomescape.web.dto.request;

public record LoginRequest(
        String loginId,
        String password
) {
}
