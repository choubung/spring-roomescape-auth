package roomescape.web.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.auth.Token;
import roomescape.domain.user.*;
import roomescape.service.AuthService;
import roomescape.web.dto.request.LoginRequest;
import roomescape.web.dto.request.SignUpRequest;
import roomescape.web.dto.response.LoginResponse;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signUp")
    public ResponseEntity<Void> signUp(
            @Valid @RequestBody SignUpRequest request
    ) {
        User user;

        if (UserRole.ADMIN.equals(request.role())) {
            user = User.createAdmin(new LoginId(request.loginId()), new Password(request.password()), UserName.from(request.name()));
        } else {
            user = User.createUser(new LoginId(request.loginId()), new Password(request.password()), UserName.from(request.name()));
        }

        authService.signUp(user);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        Token token = authService.login(new LoginId(request.loginId()), new Password(request.password()));
        LoginResponse response = new LoginResponse(token);

        return ResponseEntity.ok(response);
    }
}
