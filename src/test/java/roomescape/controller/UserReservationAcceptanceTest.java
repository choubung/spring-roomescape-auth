package roomescape.controller;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import roomescape.common.FixedClockConfig;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "JWT_SECRET_KEY=this-is-a-very-long-and-secure-secret-key-for-test-environment-32bytes"
)
@Sql(scripts = "/reservation-waiting-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(FixedClockConfig.class)
public class UserReservationAcceptanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    private String loginAndGetToken(String loginId, String password) {
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("loginId", loginId);
        loginParams.put("password", password);

        return RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginParams)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("token.value");
    }

    @Nested
    @DisplayName("사용자 예약 생성 기능")
    class ReservationCreationCases {
        @Test
        @DisplayName("새로운 예약을 생성한다.")
        void createReservationTest() {
            String userToken = loginAndGetToken("usera", "password123");
            String adminToken = loginAndGetToken("adminId", "adminpw123");

            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-06-05");
            params.put("timeId", 1L);
            params.put("themeId", 2L);

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + userToken)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(201);

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(ContentType.JSON)
                    .when().get("/admin/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(4))
                    .body("[3].id", is(4))
                    .body("[3].name", is("유저A"))
                    .body("[3].date", is("2026-06-05"))
                    .body("[3].time.id", is(1))
                    .body("[3].time.startAt", is("10:00"))
                    .body("[3].theme.id", is(2))
                    .body("[3].theme.name", is("예약없는테마"));
        }

        @Test
        @DisplayName("이전 시간에 대해서는 예약을 생성할 수 없다.")
        void pastReservationTest() {
            String token = loginAndGetToken("usera", "password123");

            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-04-05");
            params.put("timeId", 1L);
            params.put("themeId", 2L);

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        @DisplayName("예약 가능 시간을 조회한 뒤 예약을 생성하면, 잔여 타임 목록에서 제외된다.")
        void reservationFlow() {
            RestAssured.given().log().all()
                    .queryParam("date", "2026-04-28")
                    .queryParam("themeId", 2L)
                    .when().get("/times")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(3));

            String token = loginAndGetToken("userb", "password123");

            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-06-28");
            params.put("timeId", 1L);
            params.put("themeId", 2L);

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations")
                    .then().log().all()
                    .statusCode(201);
        }
    }

    @Nested
    @DisplayName("사용자 예약 조회 기능")
    class ReservationReadCases {
        @Test
        @DisplayName("토큰 주체를 기반으로 해당 사용자의 예약 목록을 조회한다.")
        void getMyReservations() {
            String token = loginAndGetToken("usera", "password123");

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(1))
                    .body("[0].name", is("유저A"));
        }

        @Test
        @DisplayName("예약과 예약 대기가 같이 조회되는지 확인한다.")
        void getMyReservationsAndWaitingTest() {
            String token = loginAndGetToken("userb", "password123");

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(2))
                    .body("[1].rank", is(2));
        }
    }

    @Nested
    @DisplayName("사용자 예약 변경 기능")
    class ReservationUpdateCases {

        @Test
        @DisplayName("예약의 날짜와 시간을 변경한다.")
        void updateReservation() {
            String token = loginAndGetToken("userb", "password123");

            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-07-01");
            params.put("timeId", 1L);

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().patch("/reservations/2")
                    .then().log().all()
                    .statusCode(200)
                    .body("date", is("2026-07-01"))
                    .body("time.id", is(1));
        }

        @Test
        @DisplayName("지난 시간으로 변경 시 400을 반환한다.")
        void updateReservationToPastTime() {
            // 💡 [수정] id=2 예약의 진짜 주인인 userb 토큰을 장착하여 타인 수정 에러(403) 우회
            String token = loginAndGetToken("userb", "password123");

            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-04-01");
            params.put("timeId", 1L);

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().patch("/reservations/2")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        @DisplayName("이미 차있는 시간으로 변경 시 409를 반환한다.")
        void updateReservationToDuplicateSlot() {
            String token = loginAndGetToken("userb", "password123");

            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-06-05");
            params.put("timeId", 1L);

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().patch("/reservations/2")
                    .then().log().all()
                    .statusCode(409);
        }

        @Test
        @DisplayName("존재하지 않는 예약 변경 시 404를 반환한다.")
        void updateNonExistentReservation() {
            // 💡 [수정] 인증을 통과한 유저가 존재하지 않는 리소스를 건드렸을 때의 404 검증을 위해 토큰 추가
            String token = loginAndGetToken("userb", "password123");

            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-07-01");
            params.put("timeId", 1L);

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().patch("/reservations/999")
                    .then().log().all()
                    .statusCode(404);
        }

        @Nested
        @DisplayName("사용자 에약 변경 형식 오류 실패 케이스")
        class ValidationExceptionCases {

            @Test
            @DisplayName("예약 변경 시 날짜가 없으면 400과 함께 date 필드 오류 메시지를 반환한다.")
            void updateWithNullDate() {
                String token = loginAndGetToken("userb", "password123");

                Map<String, Object> params = new HashMap<>();
                params.put("timeId", 1L);

                RestAssured.given().log().all()
                        .header("Authorization", "Bearer " + token)
                        .contentType(ContentType.JSON)
                        .body(params)
                        .when().patch("/reservations/2")
                        .then().log().all()
                        .statusCode(400)
                        .body(containsString("date"));
            }
        }
    }

    @Nested
    @DisplayName("사용자 예약 삭제 기능")
    class ReservationDeletionCases {

        @Test
        @DisplayName("미래 예약을 취소한다.")
        void cancelFutureReservation() {
            String token = loginAndGetToken("userb", "password123");

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .when().delete("/reservations/2")
                    .then().log().all()
                    .statusCode(204);
        }

        @Test
        @DisplayName("확정된 예약을 취소하면, 해당 슬롯의 대기열 1순위자가 예약자로 자동 승격된다.")
        void cancelReservationAndPromoteWaiting() {
            String userToken = loginAndGetToken("userb", "password123");
            String adminToken = loginAndGetToken("adminId", "adminpw123");

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + userToken)
                    .when().delete("/reservations/2")
                    .then().log().all()
                    .statusCode(204);

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + adminToken)
                    .when().get("/admin/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(3))
                    .body("[2].name", is("유저D"));
        }

        @Test
        @DisplayName("지난 예약 취소 시 400을 반환한다.")
        void cancelPastReservation() {
            // 💡 [수정] id=1 예약의 주인은 usera이므로 토큰 일치화 수행
            String token = loginAndGetToken("usera", "password123");

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .when().delete("/reservations/1")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        @DisplayName("존재하지 않는 예약 취소 시 404를 반환한다.")
        void cancelNonExistentReservation() {
            String token = loginAndGetToken("userb", "password123");

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .when().delete("/reservations/999")
                    .then().log().all()
                    .statusCode(404);
        }
    }
}
