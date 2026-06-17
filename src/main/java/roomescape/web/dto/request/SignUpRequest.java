package roomescape.web.dto.request;

public record SignUpRequest(
        String loginId,
        String password,
        String name,
        String role
) {
}
