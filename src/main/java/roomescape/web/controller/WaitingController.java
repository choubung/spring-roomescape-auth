package roomescape.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.domain.user.User;
import roomescape.domain.user.UserName;
import roomescape.domain.reservation.ReservationWaiting;
import roomescape.service.AuthService;
import roomescape.service.WaitingCommandService;
import roomescape.service.WaitingQueryService;
import roomescape.web.common.LoginUser;
import roomescape.web.dto.request.WaitingRequest;
import roomescape.web.dto.response.WaitingResponse;

import java.net.URI;

@RestController
@RequestMapping("/reservations/waitings")
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingCommandService waitingCommandService;

    @PostMapping
    ResponseEntity<WaitingResponse> createWaiting(
            @LoginUser User user,
            @Valid @RequestBody WaitingRequest request
    ) {
        ReservationWaiting waiting = waitingCommandService.create(user, WaitingRequest.toCommand(request));

        WaitingResponse waitingResponse = WaitingResponse.from(waiting);

        Long savedId = waitingResponse.id();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedId)
                .toUri();

        return ResponseEntity.created(location).body(waitingResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelWaiting(
            @PathVariable Long id,
            @LoginUser User user
    ) {
        waitingCommandService.cancel(id, user);
        return ResponseEntity.noContent().build();
    }
}
