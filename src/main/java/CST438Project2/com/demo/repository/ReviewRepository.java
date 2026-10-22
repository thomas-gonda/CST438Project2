
package CST438Project2.com.demo.repository;

import CST438Project2.com.demo.model.Review;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Review save(Review review) {
        String sql = """
            INSERT INTO "REVIEW"
                ("reviewDate", "comment", "rating", "userId", "gameId")
            VALUES (?, ?, ?, ?, ?)
            """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    sql, new String[]{"id"}
            );

            statement.setDate(1, Date.valueOf(review.getReviewDate()));
            statement.setString(2, review.getComment());
            statement.setFloat(3, review.getRating());
            statement.setLong(4, review.getUserId());
            statement.setLong(5, review.getGameId());

            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        if (generatedId == null) {
            throw new IllegalStateException(
                    "The database did not return an id for the new review"
            );
        }

        return new Review(
                generatedId.longValue(),
                review.getReviewDate(),
                review.getComment(),
                review.getRating(),
                review.getUserId(),
                review.getGameId()
        );
    }

    // updates a review's comment and rating in the database
    public void update(Review review) {
        String sql = """
        UPDATE "REVIEW"
        SET "comment" = ?, "rating" = ?
        WHERE "id" = ?
        """;

        jdbcTemplate.update(
                sql,
                review.getComment(),
                review.getRating(),
                review.getId()
        );
    }

    public List<Review> findByGameId(Long gameId) {
        String sql = """
            SELECT "id", "reviewDate", "comment", "rating", "userId", "gameId"
            FROM "REVIEW"
            WHERE "gameId" = ?
            """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new Review(
                        rs.getLong("id"),
                        rs.getDate("reviewDate").toLocalDate(),
                        rs.getString("comment"),
                        rs.getFloat("rating"),
                        rs.getLong("userId"),
                        rs.getLong("gameId")
                ),
                gameId
        );
    }

    /**
     * Retrieves one page of reviews for a game ordered by review id.
     *
     * @param gameId game whose reviews should be returned
     * @param page zero-based page number
     * @param size maximum number of reviews
     * @return reviews on the requested page
     */
    public List<Review> findPageByGameId(Long gameId, int page, int size) {
        return findPage("gameId", gameId, page, size);
    }

    /**
     * Retrieves one page of reviews for a user ordered by review id.
     *
     * @param userId user whose reviews should be returned
     * @param page zero-based page number
     * @param size maximum number of reviews
     * @return reviews on the requested page
     */
    public List<Review> findPageByUserId(Long userId, int page, int size) {
        return findPage("userId", userId, page, size);
    }

    /**
     * Counts reviews belonging to a game.
     *
     * @param gameId game whose reviews should be counted
     * @return number of reviews
     */
    public long countByGameId(Long gameId) {
        return countBy("gameId", gameId);
    }

    /**
     * Counts reviews belonging to a user.
     *
     * @param userId user whose reviews should be counted
     * @return number of reviews
     */
    public long countByUserId(Long userId) {
        return countBy("userId", userId);
    }

    private List<Review> findPage(String column, Long value, int page, int size) {
        long offset = (long) page * size;
        String sql = """
            SELECT "id", "reviewDate", "comment", "rating", "userId", "gameId"
            FROM "REVIEW"
            WHERE "%s" = ?
            ORDER BY "id"
            LIMIT ? OFFSET ?
            """.formatted(column);

        return jdbcTemplate.query(sql, this::mapReview, value, size, offset);
    }

    private long countBy(String column, Long value) {
        String sql = "SELECT COUNT(*) FROM \"REVIEW\" WHERE \"%s\" = ?"
                .formatted(column);
        Long count = jdbcTemplate.queryForObject(sql, Long.class, value);
        return count == null ? 0 : count;
    }

    /**
     * Deletes a review by id.
     *
     * @param reviewId review to delete
     */
    public void deleteById(Long reviewId) {
        jdbcTemplate.update(
                "DELETE FROM \"REVIEW\" WHERE \"id\" = ?", reviewId);
    }

    public Optional<Review> findById(Long reviewId) {
        String sql = """
            SELECT "id", "reviewDate", "comment", "rating", "userId", "gameId"
            FROM "REVIEW"
            WHERE "id" = ?
            """;

        return jdbcTemplate.query(
                sql,
                this::mapReview,
                reviewId
        ).stream().findFirst();
    }

    private Review mapReview(java.sql.ResultSet rs, int rowNum)
            throws java.sql.SQLException {
        return new Review(
                rs.getLong("id"),
                rs.getDate("reviewDate").toLocalDate(),
                rs.getString("comment"),
                rs.getFloat("rating"),
                rs.getLong("userId"),
                rs.getLong("gameId"));
    }
}
