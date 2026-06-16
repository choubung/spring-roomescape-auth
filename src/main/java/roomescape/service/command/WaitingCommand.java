package roomescape.service.command;

import roomescape.domain.user.UserName;

import java.time.LocalDate;

public record WaitingCommand(
        UserName name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
}
