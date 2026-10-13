
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

    public Optional<Review> findById(Long reviewId) {
        String sql = """
            SELECT "id", "reviewDate", "comment", "rating", "userId", "gameId"
            FROM "REVIEW"
            WHERE "id" = ?
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
                reviewId
        ).stream().findFirst();
    }
}
