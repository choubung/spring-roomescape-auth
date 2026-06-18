package roomescape.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.domain.user.User;
import roomescape.domain.user.UserName;
import roomescape.service.ReservationUserCommandService;
import roomescape.service.ReservationQueryService;
import roomescape.service.WaitingQueryService;
import roomescape.web.common.LoginUser;
import roomescape.web.dto.request.ReservationRequest;
import roomescape.web.dto.request.ReservationUpdateRequest;
import roomescape.web.dto.response.ReservationResponse;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationUserCommandService reservationUserCommandService;
    private final ReservationQueryService reservationQueryService;
    private final WaitingQueryService waitingQueryService;

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @LoginUser User user
    ) {
        List<ReservationResponse> responses = Stream.concat(
                reservationQueryService.getByName(user.getName()).stream().map(ReservationResponse::from),
                waitingQueryService.getByName(user.getName()).stream().map(ReservationResponse::from)
        ).toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @LoginUser User user,
            @Valid @RequestBody ReservationRequest request
    ) {
        ReservationResponse reservationResponse = ReservationResponse.from(
                reservationUserCommandService.create(user, ReservationRequest.toCommand(request)));

        Long savedId = reservationResponse.id();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedId)
                .toUri();

        return ResponseEntity.created(location).body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable Long id,
            @LoginUser User user
    ) {
        reservationUserCommandService.cancel(id, user.getName());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @LoginUser User user,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        ReservationResponse response = ReservationResponse.from(
                reservationUserCommandService.update(id, user.getName(), ReservationUpdateRequest.toCommand(request)));
        return ResponseEntity.ok(response);
    }
}
