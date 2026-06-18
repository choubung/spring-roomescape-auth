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

// 💡 외부 .env 대신 테스트 런타임에 주입될 임시 JWT 비밀키 설정 부착
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "JWT_SECRET_KEY=this-is-a-very-long-and-secure-secret-key-for-test-environment-32bytes"
)
@Import(FixedClockConfig.class)
@Sql(scripts = "/reservation-waiting-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AdminThemeAcceptanceTest {

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
    @DisplayName("테마 관리(생성, 조회, 삭제) 기능")
    class ThemeManagementCases {

        @Test
        @DisplayName("새로운 테마를 생성한다.")
        void createTheme() {
            Map<String, String> params = new HashMap<>();
            params.put("name", "공포");
            params.put("thumbnailUrl", "test_url");
            params.put("description", "공포_설명");

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + adminToken) // 💡 어드민 인증 헤더 부착
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/themes")
                    .then().log().all()
                    .statusCode(201)
                    .body("name", is("공포"))
                    .body("thumbnailUrl", is("test_url"))
                    .body("description", is("공포_설명"));
        }

        @Test
        @DisplayName("등록된 모든 테마를 조회한다.")
        void readThemes() {
            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + adminToken) // 💡 어드민 인증 헤더 부착
                    .when().get("/admin/themes")
                    .then().log().all()
                    .statusCode(200)
                    .body("size()", is(2));
        }

        @Test
        @DisplayName("연결된 예약이 없는 테마는 삭제할 수 있다.")
        void deleteThemeWithoutReservation() {
            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + adminToken) // 💡 어드민 인증 헤더 부착
                    .when().delete("/admin/themes/2")
                    .then().log().all()
                    .statusCode(204);
        }

        @Test
        @DisplayName("연결된 예약이 있는 테마를 삭제하려 하면 400 에러가 발생한다.")
        void deleteThemeWithReservation() {
            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + adminToken) // 💡 어드민 인증 헤더 부착
                    .when().delete("/admin/themes/1")
                    .then().log().all()
                    .statusCode(400);
        }
    }

    @Nested
    @DisplayName("관리자 테마 생성 실패 케이스")
    class ValidationExceptionCases {

        @Test
        @DisplayName("테마 생성 시 이름이 없으면 400과 함께 name 필드 오류 메시지를 반환한다.")
        void createThemeWithBlankName() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "");
            params.put("thumbnailUrl", "https://example.com/img.jpg");
            params.put("description", "설명");

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + adminToken) // 💡 어드민 인증 헤더 부착
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/themes")
                    .then().log().all()
                    .statusCode(400)
                    .body(containsString("name"));
        }

        @Test
        @DisplayName("테마 생성 시 이름이 공백만 있으면 400과 함께 name 필드 오류 메시지를 반환한다.")
        void createThemeWithWhitespaceName() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "   ");
            params.put("thumbnailUrl", "https://example.com/img.jpg");
            params.put("description", "설명");

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + adminToken) // 💡 어드민 인증 헤더 부착
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/themes")
                    .then().log().all()
                    .statusCode(400)
                    .body(containsString("name"));
        }

        @Test
        @DisplayName("테마 생성 시 thumbnailUrl과 description이 없어도 성공한다.")
        void createThemeWithoutOptionalFields() {
            Map<String, Object> params = new HashMap<>();
            params.put("name", "공포의 방");

            RestAssured.given().log().all()
                    .header("Authorization", "Bearer " + adminToken) // 💡 어드민 인증 헤더 부착
                    .contentType(ContentType.JSON)
                    .body(params)
                    .when().post("/admin/themes")
                    .then().log().all()
                    .statusCode(201);
        }
    }
}
