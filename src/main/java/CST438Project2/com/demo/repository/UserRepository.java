
package CST438Project2.com.demo.repository;

import CST438Project2.com.demo.model.User;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User save(User user) {
        String sql = """
            INSERT INTO "USER" ("displayName", "email", "role")
            VALUES (?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql, new String[]{"id"}
            );

            statement.setString(1, user.getDisplayName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getRole());

            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        if (generatedId == null) {
            throw new IllegalStateException(
                    "The database did not return an id for the new user"
            );
        }

        return new User(
                generatedId.longValue(),
                user.getDisplayName(),
                user.getEmail(),
                user.getRole()
        );
    }

    public List<User> findAll() {
        String sql = """
            SELECT "id", "displayName", "email", "role"
            FROM "USER"
            ORDER BY "id"
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new User(
                        rs.getLong("id"),
                        rs.getString("displayName"),
                        rs.getString("email"),
                        rs.getString("role")
                )
        );
    }

    public Optional<User> findById(Long userId) {
        String sql = """
            SELECT "id", "displayName", "email", "role"
            FROM "USER"
            WHERE "id" = ?
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new User(
                        rs.getLong("id"),
                        rs.getString("displayName"),
                        rs.getString("email"),
                        rs.getString("role")
                ),
                userId
        ).stream().findFirst();
    }

    public Optional<User> update(
            Long userId,
            String displayName,
            String email,
            String role) {

        Optional<User> existing = findById(userId);

        if (existing.isEmpty()) {
            return Optional.empty();
        }

        User user = existing.get();

        if (displayName != null) {
            user.setDisplayName(displayName.trim());
        }

        if (email != null) {
            user.setEmail(email.trim());
        }

        if (role != null) {
            user.setRole(role.trim());
        }

        String sql = """
            UPDATE "USER"
            SET "displayName" = ?, "email" = ?, "role" = ?
            WHERE "id" = ?
            """;

        jdbcTemplate.update(
                sql,
                user.getDisplayName(),
                user.getEmail(),
                user.getRole(),
                userId
        );

        return Optional.of(user);
    }

    public boolean deleteById(Long userId) {
        String sql = """
            DELETE FROM "USER"
            WHERE "id" = ?
            """;

        int rowsAffected = jdbcTemplate.update(sql, userId);

        return rowsAffected > 0;
    }
}
