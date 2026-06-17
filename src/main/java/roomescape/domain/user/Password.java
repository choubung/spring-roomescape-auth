package roomescape.domain.user;

public record Password(
        String value
) {
    public Password {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 비어있을 수 없습니다.");
        }
    }
}
