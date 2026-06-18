package roomescape.domain.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationWaiting;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class WaitingPromotionService {
    private final WaitingRanker waitingRanker;

    public Optional<WaitingPromotionResult> promote(List<ReservationWaiting> waitings) {
        if (waitings.isEmpty()) {
            return Optional.empty();
        }

        ReservationWaiting earliestWaiting = waitingRanker.getEarliestWaiting(waitings);
        Reservation promotedReservation = reservationFrom(earliestWaiting);

        return Optional.of(new WaitingPromotionResult(earliestWaiting, promotedReservation));
    }

    private Reservation reservationFrom(ReservationWaiting waiting) {
        return Reservation.promote(
                waiting.getUserName(),
                waiting.getSlot()
        );
    }
}
