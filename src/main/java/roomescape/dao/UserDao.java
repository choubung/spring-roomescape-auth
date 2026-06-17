package roomescape.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import roomescape.domain.user.*;

import java.util.Optional;

@Repository
public class UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert insertExecutor;

    private final RowMapper<User> rowMapper = (rs, rowNum) ->
            User.from(
                    rs.getLong("id"),
                    new LoginId(rs.getString("login_id")),
                    new Password(rs.getString("password")),
                    UserName.from(rs.getString("name")),
                    UserRole.valueOf(rs.getString("role"))
            );

    public UserDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertExecutor = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");
    }

    public Long create(User user) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("login_id", user.getLoginId().value())
                .addValue("password", user.getPassword().value())
                .addValue("name", user.getName().value())
                .addValue("role", user.getRole().name());

        Number newId = insertExecutor.executeAndReturnKey(params);

        return newId.longValue();
    }

    public Optional<User> findById(Long id) {
        String sql = "SELECT id, login_id, password, name, role FROM users WHERE id = ?";
        return jdbcTemplate.query(sql, rowMapper, id)
                .stream()
                .findFirst();
    }

    public Optional<User> findByLoginId(LoginId loginId) {
        String sql = "SELECT id, login_id, password, name, role FROM users WHERE login_id = ?";
        return jdbcTemplate.query(sql, rowMapper, loginId.value())
                .stream()
                .findFirst();
    }

    public boolean existsByLoginId(LoginId loginId) {
        String sql = """
            SELECT COUNT(1)
            FROM users
            WHERE login_id = ?
            """;

        Integer count = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                loginId.value()
        );

        return count != null && count > 0;
    }
}
