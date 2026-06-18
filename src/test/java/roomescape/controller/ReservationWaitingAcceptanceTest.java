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

import static org.hamcrest.Matchers.is;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "JWT_SECRET_KEY=this-is-a-very-long-and-secure-secret-key-for-test-environment-32bytes"
)
@Sql(scripts = "/reservation-waiting-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(FixedClockConfig.class)
public class ReservationWaitingAcceptanceTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    /**
     * 🔐 SQL에 저장된 특정 유저의 loginId로 토큰을 획득하는 헬퍼 메서드
     */
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
    @DisplayName("예약 대기 신청 기능")
    class WaitingCreationCases {

        @Test
        @DisplayName("기존 예약이 존재할 때 예약 대기가 성공적으로 되는지 확인한다.")
        void createWaitingTest() {
            // 💡 [수정] 정규식 정책 및 SQL에 맞춰 usera / password123 으로 정정
            String token = loginAndGetToken("usera", "password123");

            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-06-05");
            params.put("timeId", 1L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(201);
        }

        @Test
        @DisplayName("예약 대기가 성공적으로 취소되는지 확인한다.")
        void cancelWaitingTest() {
            // 💡 [수정] user_d -> userd, 패스워드 정정
            String token = loginAndGetToken("userd", "password123");

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .when().delete("/reservations/waitings/2")
                    .then().log().all()
                    .statusCode(204);
        }

        @Test
        @DisplayName("기존 예약이 존재하지 않으면 예약 대기가 실패한다.")
        void createWaitingWithoutReservationTest() {
            // 💡 [수정] usera / password123 으로 정정
            String token = loginAndGetToken("usera", "password123");

            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-06-06");
            params.put("timeId", 1L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(404);
        }

        @Test
        @DisplayName("같은 날짜/시간/테마에 여러 개의 예약 대기를 생성할 수 없다.")
        void createDuplicateWaitingTest() {
            // 💡 [수정] user_d -> userd, 패스워드 정정
            String token = loginAndGetToken("userd", "password123");

            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-06-05");
            params.put("timeId", 2L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(409);
        }

        @Test
        @DisplayName("기존 예약자와 같은 이름으로 예약 대기를 생성할 수 없다.")
        void createWaitingWithMyReservationTest() {
            // 💡 [수정] user_c -> userc, 패스워드 정정
            String token = loginAndGetToken("userc", "password123");

            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-06-05");
            params.put("timeId", 1L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(409);
        }

        @Test
        @DisplayName("지나간 시간에는 예약 대기를 생성할 수 없다.")
        void createPastWaitingTest() {
            // 💡 [수정] usera / password123 으로 정정
            String token = loginAndGetToken("usera", "password123");

            Map<String, Object> params = new HashMap<>();
            params.put("date", "2026-04-28");
            params.put("timeId", 1L);
            params.put("themeId", 1L);

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/reservations/waitings")
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("예약 대기 취소 기능")
    class WaitingCancellationCases {

        @Test
        @DisplayName("이미 시작된 게임의 예약 대기는 취소할 수 없다.")
        void cancelPastWaitingTest() {
            // 💡 [수정] user_d -> userd, 패스워드 정정
            String token = loginAndGetToken("userd", "password123");

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + token)
                    .when().delete("/reservations/waitings/1")
                    .then().log().all()
                    .statusCode(400);
        }

        @Test
        @DisplayName("타인의 예약 대기는 취소할 수 없다.")
        void cancelOtherWaitingTest() {
            // 💡 [수정] 언더바 소거 및 패스워드 정정
            String otherUserToken = loginAndGetToken("usera", "password123");

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + otherUserToken)
                    .when().delete("/reservations/waitings/2")
                    .then().log().all()
                    .statusCode(403);
        }

        @Test
        @DisplayName("중간 순번의 대기자가 취소하면, 뒤에 남은 대기자들의 순번이 자동으로 하나씩 당겨진다.")
        void reorderWaitingRankAfterCancellation() {
            String tokenF = loginAndGetToken("userf", "password123");
            String tokenB = loginAndGetToken("userb", "password123");

            // 1. 취소 전: userf의 상태가 "예약대기"이고 rank가 3인지 확인
            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + tokenF)
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("[0].status", is("예약대기")) // 💡 상태 검증 먼저 얹기
                    .body("[0].rank", is(3));

            // 2. When: 대기 2번인 userb가 본인의 대기(id=2)를 취소
            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + tokenB)
                    .when().delete("/reservations/waitings/2")
                    .then().log().all()
                    .statusCode(204);

            // 3. Then: 대기 3번이었던 userf의 대기 순번이 2등으로 성공적으로 당겨짐!
            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + tokenF)
                    .when().get("/reservations")
                    .then().log().all()
                    .statusCode(200)
                    .body("[0].status", is("예약대기")) // 💡 여전히 대기 상태인지 확인
                    .body("[0].rank", is(2));
        }
    }
}
