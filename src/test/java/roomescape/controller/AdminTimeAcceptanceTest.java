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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

// 💡 환경 변수 누락으로 인한 컨텍스트 로딩 실패를 막기 위한 테스트 프로퍼티 지정
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "JWT_SECRET_KEY=this-is-a-very-long-and-secure-secret-key-for-test-environment-32bytes"
)
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-waiting-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminTimeAcceptanceTest {

    @LocalServerPort
    private int port;

    private String adminToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("loginId", "adminId");
        loginParams.put("password", "adminpw123"); // 💡 특수문자나 대소문자 혼용으로 인한 VO 예외 방지를 위해 평탄화 및 SQL 데이터와 동기화

        adminToken = RestAssured.given().log().all()
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
    @DisplayName("예약 시간 관리(생성, 조회, 삭제) 기능")
    class ReservationTimeManagementCases {

        @Test
        @DisplayName("새로운 예약 시간을 생성한다.")
        void createTime() { // 💡 기존 메서드명 오타 정정 (createTheme -> createTime)
            Map<String, String> params = new HashMap<>();
            params.put("startAt", "11:00");

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + adminToken) // 💡 어드민 토큰 헤더 추가
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/times")
                    .then().log().all()
                    .statusCode(201)
                    .body("startAt", is("11:00"));
        }

        @Test
        @DisplayName("등록된 모든 예약 시간을 조회한다.")
        void readTimes() { // 💡 기존 메서드명 오타 정정 (readThemes -> readTimes)
            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + adminToken) // 💡 어드민 토큰 헤더 추가
                    .when().get("/admin/times")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(3));
        }


        @Test
        @DisplayName("연결된 예약이 없는 예약 시간은 삭제할 수 있다.")
        void deleteTimeWithoutReservation() { // 💡 기존 메서드명 오타 정정 (deleteThemeWithoutReservation -> deleteTimeWithoutReservation)
            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + adminToken) // 💡 어드민 토큰 헤더 추가
                    .when().delete("/admin/times/3")
                    .then().log().all()
                    .statusCode(204);
        }

        @Test
        @DisplayName("연결된 예약이 있는 테마를 삭제하려 하면 400 에러가 발생한다.")
        void deleteTimeWithReservation() { // 💡 기존 메서드명 오타 정정 (deleteThemeWithReservation -> deleteTimeWithReservation)
            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + adminToken) // 💡 어드민 토큰 헤더 추가
                    .when().delete("/admin/times/1")
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("예약 시간 생성 실패 케이스")
    class ValidationExceptionCases {
        @Test
        @DisplayName("startAt이 없으면 400과 함께 startAt 필드 오류 메시지를 반환한다.")
        void createTimeWithNullStartAt() {
            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + adminToken) // 💡 어드민 토큰 헤더 추가
                    .contentType(ContentType.JSON)
                    .body(Collections.emptyMap())
                    .when().post("/admin/times")
                    .then().log().all()
                    .statusCode(400)
                    .body(containsString("startAt"));
        }

        @Test
        @DisplayName("startAt 형식이 잘못되면 400과 함께 startAt 필드 오류 메시지를 반환한다.")
        void createTimeWithInvalidStartAtFormat() {
            Map<String, Object> params = new HashMap<>();
            params.put("startAt", "25:00");

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + adminToken) // 💡 어드민 토큰 헤더 추가
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/times")
                    .then().log().all()
                    .statusCode(400)
                    .body(containsString("startAt"));
        }
    }
}
