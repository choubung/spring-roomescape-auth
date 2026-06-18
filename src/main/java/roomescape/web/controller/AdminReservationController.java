package roomescape.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.user.User;
import roomescape.service.AuthService;
import roomescape.service.ReservationAdminCommandService;
import roomescape.service.ReservationUserCommandService;
import roomescape.service.ReservationQueryService;
import roomescape.web.common.LoginUser;
import roomescape.web.dto.request.ReservationRequest;
import roomescape.web.dto.response.ReservationResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/reservations")
@RequiredArgsConstructor
public class AdminReservationController {

    private final ReservationAdminCommandService reservationAdminCommandService;
    private final ReservationUserCommandService reservationUserCommandService;
    private final ReservationQueryService reservationQueryService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations(
            @LoginUser User user
    ) {
        authService.validateAdmin(user);
        List<Reservation> allReservations = reservationQueryService.getAllReservations();

        List<ReservationResponse> reservationResponses = allReservations.stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(reservationResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @LoginUser User user,
            @Valid @RequestBody ReservationRequest request
    ) {
        authService.validateAdmin(user);
        Reservation reservation = reservationUserCommandService.create(user, ReservationRequest.toCommand(request));

        Long savedId = reservation.getId();
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedId)
                .toUri();

        return ResponseEntity.created(location).body(ReservationResponse.from(reservation));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @LoginUser User user,
            @PathVariable Long id
    ) {
        authService.validateAdmin(user);
        reservationAdminCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
