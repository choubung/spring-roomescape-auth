package roomescape;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = "JWT_SECRET_KEY=this-is-a-very-long-and-secure-secret-key-for-test-environment-32bytes"
)
class RoomescapeApplicationTest {

        @Test
        void contextLoads() {
        }
}
