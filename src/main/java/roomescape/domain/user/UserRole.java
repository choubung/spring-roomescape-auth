package roomescape.domain.user;

public enum UserRole {
    ADMIN, USER;

    public static UserRole from(String role) {
        try {
            return UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("올바르지 않은 권한입니다: " + role);
        }
    }
}
