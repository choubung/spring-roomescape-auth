package roomescape.domain.user;

public record Password(
        String password
) {
    public Password {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 비어있을 수 없습니다.");
        }
    }
}
