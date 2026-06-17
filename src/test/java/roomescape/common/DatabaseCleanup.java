package roomescape.common;import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.Collectors;

@Component
@ActiveProfiles("test")
public class DatabaseCleanup implements InitializingBean {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private List<String> tableNames;

    @Override
    public void afterPropertiesSet() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
                String.class
        );

        this.tableNames = tables.stream()
                .filter(name -> !name.equalsIgnoreCase("flyway_schema_history"))
                .filter(name -> !name.equalsIgnoreCase("theme"))
                .filter(name -> !name.equalsIgnoreCase("reservation_time"))
                .collect(Collectors.toList());
    }

    public void execute() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

        for (String tableName : tableNames) {
            jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ALTER COLUMN ID RESTART WITH 1");
        }

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
    }
}
