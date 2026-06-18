package roomescape.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.user.User;
import roomescape.service.AuthService;
import roomescape.service.ReservationTimeCommandService;
import roomescape.service.ReservationTimeQueryService;
import roomescape.web.common.LoginUser;
import roomescape.web.dto.request.ReservationTimeRequest;
import roomescape.web.dto.response.ReservationTimeResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/times")
@RequiredArgsConstructor
public class AdminTimeController {

    private final ReservationTimeCommandService reservationTimeCommandService;
    private final ReservationTimeQueryService reservationTimeQueryService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getAllTimes(
            @LoginUser User user
    ) {
        authService.validateAdmin(user);
        List<ReservationTime> allReservationTimes = reservationTimeQueryService.findAllReservationTimes();
        List<ReservationTimeResponse> reservationTimeResponse = allReservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
        return ResponseEntity.ok(reservationTimeResponse);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createReservationTime(
            @LoginUser User user,
            @Valid @RequestBody ReservationTimeRequest request
    ) {
        authService.validateAdmin(user);
        ReservationTime reservationTime = reservationTimeCommandService.create(request.startAt());
        Long savedId = reservationTime.getId();

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedId)
                .toUri();

        return ResponseEntity.created(location)
                .body(ReservationTimeResponse.from(reservationTime));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(
            @LoginUser User user,
            @PathVariable Long id
    ) {
        authService.validateAdmin(user);
        reservationTimeCommandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
