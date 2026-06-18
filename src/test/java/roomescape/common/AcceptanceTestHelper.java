package roomescape.common;

import io.restassured.RestAssured;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

public class AcceptanceTestHelper {

    // 💡 회원가입 및 로그인을 수행하고 Access Token을 반환하는 메서드
    protected static String loginAndGetToken(String loginId, String password, String name, String role) {
        // 1. 회원가입 진행
        Map<String, String> signUpParams = new HashMap<>();
        signUpParams.put("loginId", loginId);
        signUpParams.put("password", password);
        signUpParams.put("name", name);
        signUpParams.put("role", role); // "USER" 또는 "ADMIN"

        RestAssured.given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(signUpParams)
                .when().post("/signUp") // 컨트롤러 스펙 매칭
                .then().log().all()
                .statusCode(201);

        // 2. 로그인 진행 및 토큰 추출
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
                .getString("token.accessToken"); // ⚠️ 만약 토큰 필드명이 다르다면 이 경로만 살짝 수정해줘!
    }
}
