package roomescape.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.FixedClockConfig;

import static org.hamcrest.Matchers.is;

// 💡 포트 설정을 RANDOM_PORT로 통일하고 JWT 임시 프로퍼티를 주입하여 빈 생성 오류를 철벽 방어합니다.
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "JWT_SECRET_KEY=this-is-a-very-long-and-secure-secret-key-for-test-environment-32bytes"
)
@Import(FixedClockConfig.class)
@Sql(scripts = "/popular-theme-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserReservationTimeAcceptanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Nested
    @DisplayName("시간이 2개일 때, 예약 가능한 모든 시간 조회 기능")
    class AvailableTimeCases {

        @Test
        @DisplayName("예약이 없는 날짜의 시간을 조회한다.")
        void readAvailableTime() {
            RestAssured.given().log().all()
                    .queryParam("date", "2027-05-03")
                    .queryParam("themeId", 7L)
                    .when().get("/times")
                    .then()
                    .statusCode(200).log().all()
                    .body("size()", is(2)); // 세미콜론 중복 오타 슥 정리 완료
        }

        @Test
        @DisplayName("예약이 하나 존재하는 날짜의 시간을 조회한다.")
        void readAvailableTimeWithExistReservation() {
            // popular-theme-test-data.sql을 보면 2026-05-03에 theme_id=7, time_id=1 예약이 딱 1건 걸려있음!
            // 전체 2개 타임 중 1개가 제외되므로 남은 1개만 조회되는 비즈니스 로직을 완벽히 검증합니다.
            RestAssured.given().log().all()
                    .queryParam("date", "2026-05-03")
                    .queryParam("themeId", 7L)
                    .when().get("/times")
                    .then()
                    .statusCode(200).log().all()
                    .body("size()", is(1));
        }
    }
}
