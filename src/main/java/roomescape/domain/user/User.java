package roomescape.domain.user;

import java.util.Objects;

public class User {
    private final Long id;
    private final LoginId loginId;
    private final Password password;
    private final UserName name;
    private final UserRole role;

    private User(Long id, LoginId loginId, Password password, UserName name, UserRole role) {
        validateFields(loginId, password, name, role);
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public static User from(Long id, LoginId loginId, Password password, UserName name, UserRole role) {
        Objects.requireNonNull(id, "유저 식별을 위한 id는 null일 수 없습니다.");
        return new User(id, loginId, password, name, role);
    }

    public static User createUser(LoginId loginId, Password password, UserName name) {
        return new User(null, loginId, password, name, UserRole.USER);
    }

    public static User createAdmin(LoginId loginId, Password password, UserName name) {
        return new User(null, loginId, password, name, UserRole.ADMIN);
    }

    private void validateFields(LoginId loginId, Password password, UserName name, UserRole role) {
        Objects.requireNonNull(loginId, "loginId는 null일 수 없습니다.");
        Objects.requireNonNull(password, "password는 null일 수 없습니다.");
        Objects.requireNonNull(name, "name는 null일 수 없습니다.");
        Objects.requireNonNull(role, "role은 null일 수 없습니다.");
    }

    public Long getId() {
        return id;
    }

    public LoginId getLoginId() {
        return loginId;
    }

    public UserName getName() {
        return name;
    }

    public Password getPassword() {
        return password;
    }

    public UserRole getRole() {
        return role;
    }
}
