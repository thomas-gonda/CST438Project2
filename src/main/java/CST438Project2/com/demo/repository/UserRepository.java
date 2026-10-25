
package CST438Project2.com.demo.repository;

import CST438Project2.com.demo.model.User;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

/**
 * Reads and writes users in the database.
 */
@Repository
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Saves a new user.
     *
     * @param user user to save
     * @return the saved user with its generated id
     */
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

    /**
     * Finds all users.
     *
     * @return all users ordered by id
     */
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

    /**
     * Finds one user by id.
     *
     * @param userId user id to find
     * @return the user when it exists
     */
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

    /**
     * Finds a user by the email or username represented by the authenticated principal.
     *
     * @param email authenticated principal name
     * @return the matching user, when one exists
     */
    public Optional<User> findByEmail(String email) {
        return jdbcTemplate.query(
                """
                SELECT "id", "displayName", "email", "role"
                FROM "USER"
                WHERE "email" = ?
                """,
                (rs, rowNum) -> new User(
                        rs.getLong("id"),
                        rs.getString("displayName"),
                        rs.getString("email"),
                        rs.getString("role")),
                email).stream().findFirst();
    }

    /**
     * Updates a user's editable fields.
     *
     * @param userId user id to update
     * @param displayName new display name
     * @param email new email
     * @param role new role
     * @return the updated user, or empty when it does not exist
     */
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

    /**
     * Deletes a user by id.
     *
     * @param userId user id to delete
     * @return true when a user was deleted
     */
    public boolean deleteById(Long userId) {
        String sql = """
            DELETE FROM "USER"
            WHERE "id" = ?
            """;

        int rowsAffected = jdbcTemplate.update(sql, userId);

        return rowsAffected > 0;
    }
}
