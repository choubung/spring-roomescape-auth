package roomescape.controller;

import io.jsonwebtoken.Jwts;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import roomescape.common.DatabaseCleanup;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = AuthAcceptanceTest.PropertyInitializer.class)
class AuthAcceptanceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private DatabaseCleanup databaseCleanup;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        databaseCleanup.execute();
    }

    @Test
    @DisplayName("회원가입 후 로그인을 진행하면 정상적으로 토큰을 발급받는다.")
    void auth_Flow_Success() {
        // 회원가입 요청
        Map<String, Object> signUpParams = new HashMap<>();
        signUpParams.put("loginId", "testUser123");
        signUpParams.put("password", "securePassword1!");
        signUpParams.put("name", "조로");
        signUpParams.put("role", "USER");

        ExtractableResponse<Response> signUpResponse = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(signUpParams)
                .when()
                .post("/signUp")
                .then().log().all()
                .extract();


        System.out.println("❌ 서버가 보낸 에러 상세: " + signUpResponse.body().asString());

        assertThat(signUpResponse.statusCode()).isEqualTo(HttpStatus.CREATED.value());

        // 로그인 요청
        Map<String, Object> loginParams = new HashMap<>();
        loginParams.put("loginId", "testUser123");
        loginParams.put("password", "securePassword1!");

        ExtractableResponse<Response> loginResponse = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(loginParams)
                .when()
                .post("/login")
                .then().log().all()
                .extract();

        assertThat(loginResponse.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(loginResponse.jsonPath().getString("token.value")).isNotNull();
    }

    @Test
    @DisplayName("이미 존재하는 아이디로 회원가입을 시도하면 400 Bad Request(혹은 409 Conflict) 에러가 발생한다.")
    void signUp_Duplicate_Exception() {
        // 첫 번째 회원가입
        Map<String, Object> signUpParams = new HashMap<>();
        signUpParams.put("loginId", "duplicateId");
        signUpParams.put("password", "password123");
        signUpParams.put("name", "루피");
        signUpParams.put("role", "USER");

        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(signUpParams)
                .when()
                .post("/signUp")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // 동일한 아이디로 두 번째 회원가입 시도
        ExtractableResponse<Response> duplicateResponse = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(signUpParams)
                .when()
                .post("/signUp")
                .then().log().all()
                .extract();

        assertThat(duplicateResponse.statusCode()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    @Test
    @DisplayName("틀린 비밀번호로 로그인을 시도하면 400 Bad Request 에러가 발생한다.")
    void login_WrongPassword_Exception() {
        Map<String, Object> signUpParams = new HashMap<>();
        signUpParams.put("loginId", "loginTestUser");
        signUpParams.put("password", "correctPassword");
        signUpParams.put("name", "나미");
        signUpParams.put("role", "USER");

        RestAssured.given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(signUpParams)
                .when()
                .post("/signUp")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        // 틀린 비밀번호로 로그인 시도
        Map<String, Object> wrongLoginParams = new HashMap<>();
        wrongLoginParams.put("loginId", "loginTestUser");
        wrongLoginParams.put("password", "wrongPassword!!!!");

        ExtractableResponse<Response> loginResponse = RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(wrongLoginParams)
                .when()
                .post("/login")
                .then().log().all()
                .extract();

        assertThat(loginResponse.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    static class PropertyInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            String randomSecret = Base64.getEncoder().encodeToString(Jwts.SIG.HS256.key().build().getEncoded());

            TestPropertyValues.of(
                    "JWT_SECRET_KEY=" + randomSecret
            ).applyTo(applicationContext.getEnvironment());
        }
    }
}
